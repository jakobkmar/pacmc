package net.axay.pacmc.app.features

import io.realm.query
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
import okio.Path.Companion.toPath
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

    private lateinit var dbArchive: DbArchive

    private fun updateFromDb(): DbArchive? {
        dbArchive = realm.query<DbArchive>("name == $0", name).limit(1).first().find() ?: return null
        return dbArchive
    }

    class ResolveResult(
        val versions: List<CommonProjectVersion>,
        val dependencyVersions: List<CommonProjectVersion>,
    )

    suspend fun resolve(modSlugs: List<ModSlug>): ResolveResult {
        updateFromDb()

        val modIds = modSlugs.pmap { RepositoryApi.getBasicProjectInfo(it)?.id }.filterNotNull().toSet()

        val loader = dbArchive.readLoader()
        val minecraftVersion = dbArchive.readMinecraftVersion()

        suspend fun resolveTransitively(
            modIds: Set<ModId>,
            alreadyResolved: MutableSet<ModId>,
        ): List<CommonProjectVersion> {

            val versions = modIds.pmap { modId ->
                if (modId in alreadyResolved) return@pmap null

                val projectVersions = RepositoryApi.getProjectVersions(modId, listOf(loader), listOf(minecraftVersion))
                projectVersions?.findBest(minecraftVersion)
            }.filterNotNull()

            val dependencies = versions.pmap { version ->
                version.dependencies.pmap {
                    when (it) {
                        is CommonProjectVersion.Dependency.ProjectDependency -> it.id
                        is CommonProjectVersion.Dependency.VersionDependency -> RepositoryApi.getProjectVersion(it.id, version.modId.repository)?.modId
                    }
                }.filterNotNull()
            }.flatten().toSet()

            if (dependencies.isEmpty()) return versions

            alreadyResolved += versions.map { it.modId }
            return versions + resolveTransitively(dependencies, alreadyResolved)
        }

        val versions = mutableListOf<CommonProjectVersion>()
        val dependencyVersions = mutableListOf<CommonProjectVersion>()

        resolveTransitively(modIds, mutableSetOf()).forEach {
            (if (it.modId in modIds) versions else dependencyVersions) += it
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

    suspend fun install(
        modId: ModId,
        downloadProgress: (Double) -> Unit,
    ) = coroutineScope scope@{
        updateFromDb() ?: return@scope

        if (dbArchive.installed.any { it.id == modId.id }) return@scope

        val projectVersionsDeferred = async {
            RepositoryApi.getProjectVersions(
                modId,
                listOf(dbArchive.readLoader()),
                listOf(dbArchive.readMinecraftVersion())
            )
        }
        val projectInfoDeferred = async {
            RepositoryApi.getProject(modId)
        }

        val version = projectVersionsDeferred.await()?.maxByOrNull { it.datePublished } ?: return@scope

        val downloadFile = version.files.firstOrNull { it.primary } ?: return@scope

        val fileName = ModFile(modId.repository.shortForm, projectInfoDeferred.await()?.slug?.slug, modId.id).fileName

        ktorClient.downloadFile(
            downloadFile.url,
            dbArchive.path.toPath().resolve(fileName),
            downloadProgress
        )

        realm.write {
            dbArchive.installed.add(DbInstalledProject(modId.id))
            copyToRealm(dbArchive)
        }
    }
}
