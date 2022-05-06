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
        INSTALLED(true),
        ALREADY_INSTALLED(true),
        UPDATED(true),
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
            fun Collection<CommonProjectVersion>.installAll(isDependency: Boolean = false) {
                forEach { version ->
                    launch {
                        val key = progressKeyMap[version.modId]!!
                        val result = install(version, isDependency, semaphore) {
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
            transaction.addDependencies.installAll(true)
            transaction.update.installAll()
            transaction.updateDependencies.installAll(true)

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

        val alreadyInstalled = dbArchive.installed.find { it.readModId() == modId }

        var isUpdate = false
        val shouldDownload = if (alreadyInstalled == null) true else {
            if (alreadyInstalled.version != version.id) {
                isUpdate = true
                true
            } else {
                val fileName = fileNameDeferred.await() ?: return TransactionPartResult.NO_PROJECT_INFO
                !Environment.fileSystem.exists(dbArchive.readPath().resolve(fileName))
            }
        }

        realm.write {
            findLatest(dbArchive)!!.apply {
                val presentProject = installed.find { it.readModId() == modId }
                if (presentProject != null) {
                    if (isUpdate || (presentProject.dependency && !isDependency)) {
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

            return if (isUpdate) TransactionPartResult.UPDATED else TransactionPartResult.INSTALLED
        } else {
            return TransactionPartResult.ALREADY_INSTALLED
        }
    }

    private suspend fun uninstall(modId: ModId): TransactionPartResult {
        val archive = realm.findArchive()

        val wasPresent = realm.write {
            findLatest(archive)!!.installed.removeAll { it.readModId() == modId }
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
    ) {
        fun isEmpty() = add.isEmpty() && addDependencies.isEmpty()
            && update.isEmpty() && updateDependencies.isEmpty()
            && remove.isEmpty() && removeDependencies.isEmpty()
    }

    @JvmName("resolveWithModSlug")
    suspend fun resolve(
        modSlugs: Set<ModSlug>,
        debugMessageCallback: (String) -> Unit,
    ): Transaction {
        debugMessageCallback("resolving slugs to mod ids")
        return resolve(modSlugs.resolveIds(), debugMessageCallback)
    }

    private suspend fun resolve(
        modIds: Set<ModId>,
        debugMessageCallback: (String) -> Unit,
    ): Transaction {
        debugMessageCallback("opening database")
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

                debugMessageCallback("resolving $modId")
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

    suspend fun resolveUpdate(
        debugMessageCallback: (String) -> Unit,
    ): Transaction {
        debugMessageCallback("opening database")
        val dbArchive = realm.findArchive()

        val installedVersions = mutableMapOf<ModId, String>()
        val installedDependencyVersions = mutableMapOf<ModId, String>()
        dbArchive.installed.forEach {
            (if (it.dependency) installedDependencyVersions else installedVersions)[it.readModId()] = it.version
        }

        val resolveResult = resolve(installedVersions.keys, debugMessageCallback)

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

    class RemovalResolveResult(
        val transaction: Transaction,
        val stillNeeded: Set<ModId>,
        val notInstalled: Set<ModId>,
    )

    @JvmName("resolveRemovalWithModSlug")
    suspend fun resolveRemoval(
        modSlugs: Set<ModSlug>,
        debugMessageCallback: (String) -> Unit,
    ): RemovalResolveResult {
        debugMessageCallback("resolving slugs to mod ids")
        return resolveRemoval(modSlugs.resolveIds(), debugMessageCallback)
    }

    private suspend fun resolveRemoval(
        removeModIds: Set<ModId>,
        debugMessageCallback: (String) -> Unit,
    ): RemovalResolveResult {
        debugMessageCallback("opening database")
        val dbArchive = realm.findArchive()

        val stillInstalledProjects = dbArchive.installed
            .filter { it.readModId() !in removeModIds }

        val stillNeeded = HashSet<ModId>()
        val stillNeededMutex = Mutex()

        coroutineScope {
            suspend fun checkInstalledProject(installedProject: DbInstalledProject) {
                val version = repoApiContext {
                    it.getProjectVersion(installedProject.version, installedProject.readModId().repository)
                } ?: error("Couldn't resolve an installed project version, try refreshing the archive before removing content from it")

                version.dependencies.forEach { dependency ->
                    launch {
                        val dependencyModId = when (dependency) {
                            is CommonProjectVersion.Dependency.ProjectDependency -> dependency.id
                            is CommonProjectVersion.Dependency.VersionDependency -> repoApiContext { c ->
                                c.getProjectVersion(dependency.id, version.modId.repository)?.modId
                            } ?: error("Couldn't resolve a dependency version, try refreshing the archive before removing content from it")
                        }

                        if (dependencyModId in removeModIds) {
                            stillNeededMutex.withLock {
                                stillNeeded += dependencyModId
                            }
                        }

                        val dependencyInstalledProject = stillInstalledProjects.find { it.readModId() == dependencyModId }
                            ?: return@launch

                        checkInstalledProject(dependencyInstalledProject)
                    }
                }
            }

            stillInstalledProjects
                .filterNot { it.dependency }
                .forEach { launch { checkInstalledProject(it) } }
        }

        val remove = HashSet<ModId>()
        val removeDependencies = HashSet<ModId>()

        dbArchive.installed.forEach {
            val modId = it.readModId()
            if ((modId in removeModIds || it.dependency) && modId !in stillNeeded) {
                (if (it.dependency) removeDependencies else remove) += modId
            }
        }

        return RemovalResolveResult(
            Transaction(
                remove = remove,
                removeDependencies = removeDependencies,
            ),
            stillNeeded,
            removeModIds subtract dbArchive.installed.mapTo(HashSet()) { it.readModId() },
        )
    }

    private suspend fun Collection<ModSlug>.resolveIds(): Set<ModId> {
        return pmap { repoApiContext(CachePolicy.ONLY_FRESH) { c -> c.getBasicProjectInfo(it) }?.id }.filterNotNull().toSet()
    }

    suspend fun getInstalled(): List<DbInstalledProject> {
        val dbArchive = realm.findArchive()
        return dbArchive.installed.toList()
    }
}
