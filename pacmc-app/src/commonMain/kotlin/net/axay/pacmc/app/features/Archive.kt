package net.axay.pacmc.app.features

import io.realm.TypedRealm
import io.realm.query
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModFile
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.ModSlug
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.database.model.DbInstalledProject
import net.axay.pacmc.app.database.realm
import net.axay.pacmc.app.downloadFile
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.app.repoapi.repoApiContext
import net.axay.pacmc.app.utils.pmap
import net.axay.pacmc.repoapi.CachePolicy
import okio.Path
import kotlin.math.absoluteValue

class Archive(private val name: String) {
    companion object {
        fun getArchives() = realm.query<DbArchive>().find()
        fun getArchivesList() = getArchives().toList()

        suspend fun create(dbArchive: DbArchive) {
            realm.write {
                copyToRealm(dbArchive)
            }
        }
    }

    private fun TypedRealm.queryArchive() = query<DbArchive>("name == $0", name).first()

    private suspend fun TypedRealm.findArchive() = queryArchive().asFlow().first().obj
        ?: error("The archive '$name' is not present in the database")

    sealed class TransactionProgress<U> {
        abstract val key: U

        class Update<U>(override val key: U, val progress: Double) : TransactionProgress<U>()
        class Finished<U>(override val key: U, val result: TransactionPartResult) : TransactionProgress<U>()
    }

    enum class TransactionPartResult(val success: Boolean) {
        SUCCESS(true),
        ALREADY_INSTALLED(true),
        REMOVED(true),
        ALREADY_REMOVED(true),
        NO_PROJECT_INFO(false),
        NO_FILE(false),
    }

    suspend fun <U> applyTransaction(
        transaction: Transaction,
        semaphore: Semaphore,
        progressKeyMap: Map<ModId, U>,
        progressCallback: suspend (TransactionProgress<U>) -> Unit,
    ) {
        coroutineScope {
            fun Collection<CommonProjectVersion>.installAll() {
                forEach { version ->
                    launch {
                        val key = progressKeyMap[version.modId]!!
                        val result = install(version, false, semaphore) {
                            progressCallback(TransactionProgress.Update(key, it))
                        }
                        progressCallback(TransactionProgress.Finished(key, result))
                    }
                }
            }

            fun Collection<ModId>.uninstallAll() {
                forEach {
                    launch {
                        val key = progressKeyMap[it]!!
                        val result = uninstall(it)
                        progressCallback(TransactionProgress.Finished(key, result))
                    }
                }
            }

            transaction.add.installAll()
            transaction.addDependencies.installAll()
            transaction.update.installAll()
            transaction.updateDependencies.installAll()

            transaction.remove.uninstallAll()
            transaction.removeDependencies.uninstallAll()
        }
    }

    private suspend fun install(
        version: CommonProjectVersion,
        isDependency: Boolean,
        semaphore: Semaphore?,
        downloadProgress: suspend (Double) -> Unit,
    ): TransactionPartResult {

        val modId = version.modId
        val fileNameDeferred = CoroutineScope(Dispatchers.Default).async {
            val projectInfo = repoApiContext { it.getBasicProjectInfo(modId) } ?: return@async null
            ModFile(modId.repository.shortForm, projectInfo.slug.slug, modId.id).fileName
        }

        val dbArchive = realm.findArchive()

        val shouldDownload = if (dbArchive.installed.any { it.readModId() == modId }) {
            val fileName = fileNameDeferred.await() ?: return TransactionPartResult.NO_PROJECT_INFO
            !Environment.fileSystem.exists(dbArchive.readPath().resolve(fileName))
        } else true

        realm.write {
            findLatest(dbArchive)!!.apply {
                val presentProject = installed.find { it.readModId() == modId }
                if (presentProject != null) {
                    if (presentProject.dependency && !isDependency) {
                        installed.removeAll { it.readModId() == modId }
                    } else return@write
                }
                installed.add(DbInstalledProject(version, isDependency))
            }
        }

        if (shouldDownload) {
            val downloadFile = (version.files.firstOrNull { it.primary } ?: version.files.singleOrNull())
                ?: return TransactionPartResult.NO_FILE

            val fileName = fileNameDeferred.await() ?: return TransactionPartResult.NO_PROJECT_INFO

            semaphore?.withPermit {
                ktorClient.downloadFile(
                    downloadFile.url,
                    dbArchive.readPath().resolve(fileName),
                    downloadProgress
                )
            }

            return TransactionPartResult.SUCCESS
        } else {
            return TransactionPartResult.ALREADY_INSTALLED
        }
    }

    private suspend fun uninstall(modId: ModId): TransactionPartResult {
        lateinit var archive: DbArchive

        val wasPresent = realm.write {
            archive = queryArchive().find()!!
            archive.installed.removeAll { it.readModId() == modId }
        }

        var removedAny = false
        Environment.fileSystem.list(archive.readPath()).forEach {
            if (it.isArchiveFile() && ModFile.modIdFromPath(it) == modId) {
                removedAny = true
                Environment.fileSystem.delete(path = it, mustExist = true)
            }
        }

        return if (wasPresent || removedAny) {
            TransactionPartResult.REMOVED
        } else {
            TransactionPartResult.ALREADY_REMOVED
        }
    }

    private fun List<CommonProjectVersion>.findBest(desiredVersion: MinecraftVersion): CommonProjectVersion? =
        fold<CommonProjectVersion, Pair<CommonProjectVersion, Int>?>(null) { acc, projectVersion ->
            val distance = projectVersion.gameVersions
                .mapNotNull { it.minorDistance(desiredVersion) }
                .minOrNull()

            when {
                // the major version does not fit
                distance == null -> acc
                // the previous version does not fit but this one does
                acc == null -> projectVersion to distance
                // prefer the file closer to the desired version
                distance.absoluteValue < acc.second.absoluteValue -> projectVersion to distance
                distance.absoluteValue > acc.second.absoluteValue -> acc
                // now both files are equally close to the desired version
                // prefer the one which supports the more recent game version
                distance > 0 && acc.second < 0 -> projectVersion to distance
                distance < 0 && acc.second > 0 -> acc
                // just prefer the newer file
                projectVersion.datePublished > acc.first.datePublished -> projectVersion to distance
                else -> acc
            }
        }?.first

    private fun Path.isArchiveFile(): Boolean {
        return Environment.fileSystem.metadata(this).isRegularFile &&
            name.removeSuffix(".jar").endsWith(".pacmc")
    }

    class Transaction(
        val add: List<CommonProjectVersion> = emptyList(),
        val addDependencies: List<CommonProjectVersion> = emptyList(),
        val update: List<CommonProjectVersion> = emptyList(),
        val updateDependencies: List<CommonProjectVersion> = emptyList(),
        val remove: Set<ModId> = emptySet(),
        val removeDependencies: Set<ModId> = emptySet(),
    )

    @JvmName("resolveWithModSlug")
    suspend fun resolve(modSlugs: Set<ModSlug>): Transaction {
        val modIds = modSlugs.pmap { repoApiContext(CachePolicy.ONLY_FRESH) { c -> c.getBasicProjectInfo(it) }?.id }.filterNotNull().toSet()
        return resolve(modIds)
    }

    private suspend fun resolve(modIds: Set<ModId>): Transaction {
        val dbArchive = realm.findArchive()

        val loader = dbArchive.readLoader()
        val minecraftVersion = dbArchive.readMinecraftVersion()

        val checkedModIds = HashSet<ModId>()
        val checkedModIdsMutex = Mutex()

        val versions = mutableListOf<CommonProjectVersion>()
        val dependencyVersions = mutableListOf<CommonProjectVersion>()

        val finalListMutex = Mutex()

        coroutineScope {
            suspend fun resolveTransitively(modId: ModId, isDependency: Boolean) {

                if (!checkedModIdsMutex.withLock { checkedModIds.add(modId) }) return

                if (isDependency) {
                    launch {
                        repoApiContext(CachePolicy.ONLY_FRESH) { it.getBasicProjectInfo(modId) }
                    }
                }

                val version = repoApiContext(CachePolicy.ONLY_FRESH) {
                    it.getProjectVersions(modId, listOf(loader), listOf(minecraftVersion))
                }?.findBest(minecraftVersion) ?: return

                finalListMutex.withLock {
                    (if (!isDependency) versions else dependencyVersions) += version
                }

                version.dependencies.forEach {
                    launch {
                        val dependencyModId = when (it) {
                            is CommonProjectVersion.Dependency.ProjectDependency -> it.id
                            is CommonProjectVersion.Dependency.VersionDependency -> repoApiContext(CachePolicy.ONLY_FRESH) { c ->
                                c.getProjectVersion(it.id, version.modId.repository)?.modId
                            }
                        }

                        if (dependencyModId != null) {
                            resolveTransitively(dependencyModId, true)
                        }
                    }
                }
            }

            modIds.forEach { launch { resolveTransitively(it, false) } }
        }

        return Transaction(
            add = versions,
            addDependencies = dependencyVersions,
        )
    }

    suspend fun resolveUpdate(): Transaction {
        val dbArchive = realm.findArchive()

        val installedVersions = mutableMapOf<ModId, String>()
        val installedDependencyVersions = mutableMapOf<ModId, String>()
        dbArchive.installed.forEach {
            (if (it.dependency) installedDependencyVersions else installedVersions)[it.readModId()] = it.version
        }

        val resolveResult = resolve(installedVersions.keys)

        return Transaction(
            update = resolveResult.add
                .filter { it.modId in installedVersions && installedVersions[it.modId] != it.id },
            updateDependencies = resolveResult.addDependencies
                .filter { it.modId in installedDependencyVersions && installedDependencyVersions[it.modId] != it.id },
            addDependencies = resolveResult.addDependencies
                .filter { it.modId !in installedDependencyVersions },
            removeDependencies = installedDependencyVersions.keys - resolveResult.addDependencies.mapTo(mutableSetOf()) { it.modId },
        )
    }

    suspend fun getInstalled(): List<DbInstalledProject> {
        val dbArchive = realm.findArchive()
        return dbArchive.installed.toList()
    }
}
