package net.axay.pacmc.app.features

import io.realm.TypedRealm
import io.realm.query
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import net.axay.pacmc.app.repoapi.RepositoryApi
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.app.utils.pmap
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

    private fun TypedRealm.findArchive() = query<DbArchive>("name == $0", name).first().find()
        ?: error("The archive '$name' is not present in the database")

    class ResolveResult(
        val versions: List<CommonProjectVersion>,
        val dependencyVersions: List<CommonProjectVersion>,
    )

    suspend fun resolve(modSlugs: Set<ModSlug>): ResolveResult {
        val modIds = modSlugs.pmap { RepositoryApi.getBasicProjectInfo(it)?.id }.filterNotNull().toSet()
        return resolve(modIds)
    }

    private suspend fun resolve(modIds: Set<ModId>): ResolveResult {
        val dbArchive = realm.findArchive()

        val loader = dbArchive.readLoader()
        val minecraftVersion = dbArchive.readMinecraftVersion()

        val checkedModIds = HashSet<ModId>()
        val checkedModIdsMutex = Mutex()

        val versions = mutableListOf<CommonProjectVersion>()
        val dependencyVersions = mutableListOf<CommonProjectVersion>()

        val finalListMutex = Mutex()

        coroutineScope {
            suspend fun resolveTransitively(modId: ModId) {

                if (!checkedModIdsMutex.withLock { checkedModIds.add(modId) }) return

                val version = RepositoryApi.getProjectVersions(modId, listOf(loader), listOf(minecraftVersion))
                    ?.findBest(minecraftVersion) ?: return

                finalListMutex.withLock {
                    (if (modId in modIds) versions else dependencyVersions) += version
                }

                version.dependencies.forEach {
                    launch {
                        val dependencyModId = when (it) {
                            is CommonProjectVersion.Dependency.ProjectDependency -> it.id
                            is CommonProjectVersion.Dependency.VersionDependency -> RepositoryApi.getProjectVersion(it.id, version.modId.repository)?.modId
                        }

                        if (dependencyModId != null) {
                            resolveTransitively(dependencyModId)
                        }
                    }
                }
            }

            modIds.forEach { launch { resolveTransitively(it) } }
        }

        return ResolveResult(versions, dependencyVersions)
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

    enum class InstallResult(val success: Boolean) {
        SUCCESS(true),
        ALREADY_INSTALLED(true),
        NO_PROJECT_INFO(false),
        NO_FILE(false),
    }

    suspend fun install(
        version: CommonProjectVersion,
        isDependency: Boolean,
        downloadProgress: suspend (Double) -> Unit,
    ): InstallResult {

        val modId = version.modId
        val fileNameDeferred = CoroutineScope(Dispatchers.Default).async {
            val projectInfo = RepositoryApi.getBasicProjectInfo(modId) ?: return@async null
            ModFile(modId.repository.shortForm, projectInfo.slug.slug, modId.id).fileName
        }

        val dbArchive = realm.findArchive()

        val shouldDownload = if (dbArchive.installed.any { it.readModId() == modId }) {
            val fileName = fileNameDeferred.await() ?: return InstallResult.NO_PROJECT_INFO
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
                ?: return InstallResult.NO_FILE

            val fileName = fileNameDeferred.await() ?: return InstallResult.NO_PROJECT_INFO

            ktorClient.downloadFile(
                downloadFile.url,
                dbArchive.readPath().resolve(fileName),
                downloadProgress
            )

            return InstallResult.SUCCESS
        } else {
            return InstallResult.ALREADY_INSTALLED
        }
    }

    class UpdateResult(
        val updateVersions: List<CommonProjectVersion>,
        val updateDependencyVersions: List<CommonProjectVersion>,
        val addedDependencyVersions: List<CommonProjectVersion>,
        val removedDependencies: Set<ModId>,
    )

    suspend fun resolveUpdate(): UpdateResult {
        val dbArchive = realm.findArchive()

        val installedVersions = mutableMapOf<ModId, String>()
        val installedDependencyVersions = mutableMapOf<ModId, String>()
        dbArchive.installed.forEach {
            (if (it.dependency) installedDependencyVersions else installedVersions)[it.readModId()] = it.version
        }

        val resolveResult = resolve(installedVersions.keys)

        return UpdateResult(
            updateVersions = resolveResult.versions
                .filter { it.modId in installedVersions && installedVersions[it.modId] != it.id },
            updateDependencyVersions = resolveResult.dependencyVersions
                .filter { it.modId in installedDependencyVersions && installedDependencyVersions[it.modId] != it.id },
            addedDependencyVersions = resolveResult.dependencyVersions
                .filter { it.modId !in installedDependencyVersions },
            removedDependencies = installedDependencyVersions.keys - resolveResult.dependencyVersions.mapTo(mutableSetOf()) { it.modId },
        )
    }
}
