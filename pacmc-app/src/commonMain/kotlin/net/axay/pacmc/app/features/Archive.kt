package net.axay.pacmc.app.features

import io.realm.TypedRealm
import io.realm.query
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    suspend fun resolve(modSlugs: List<ModSlug>): ResolveResult {
        val dbArchive = realm.findArchive()

        val modIds = modSlugs.pmap { RepositoryApi.getBasicProjectInfo(it)?.id }.filterNotNull().toSet()

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

    enum class InstallResult {
        SUCCESS,
        NO_PROJECT_INFO,
        NO_FILE,
        ALREADY_INSTALLED,
    }

    suspend fun install(
        version: CommonProjectVersion,
        isDependency: Boolean,
        downloadProgress: (Double) -> Unit,
    ): InstallResult = coroutineScope scope@{

        val modId = version.modId
        val modInfoDeferred = async { RepositoryApi.getBasicProjectInfo(modId) }

        val dbArchive = realm.findArchive()
        if (dbArchive.installed.any { it.matches(modId) }) {
            return@scope InstallResult.ALREADY_INSTALLED
        }

        val downloadFile = (version.files.firstOrNull { it.primary } ?: version.files.singleOrNull())
            ?: return@scope InstallResult.NO_FILE

        val modInfo = modInfoDeferred.await()
            ?: return@scope InstallResult.NO_PROJECT_INFO
        val fileName = ModFile(modId.repository.shortForm, modInfo.slug.slug, modId.id).fileName

        ktorClient.downloadFile(
            downloadFile.url,
            dbArchive.readPath().resolve(fileName),
            downloadProgress
        )

        realm.write {
            findLatest(dbArchive)!!.installed.add(DbInstalledProject(modId, isDependency))
        }

        InstallResult.SUCCESS
    }
}
