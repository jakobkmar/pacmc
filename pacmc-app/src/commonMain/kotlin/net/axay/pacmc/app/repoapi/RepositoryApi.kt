package net.axay.pacmc.app.repoapi

import net.axay.memoire.CacheValidationConfig
import net.axay.memoire.DiskCacheConfig
import net.axay.memoire.MemoryDiskCache
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.app.ktorClientJson
import net.axay.pacmc.app.repoapi.model.CommonBasicProject
import net.axay.pacmc.app.repoapi.model.CommonProject
import net.axay.pacmc.app.repoapi.model.CommonProjectResult
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.common.data.IdOrSlug
import net.axay.pacmc.common.data.MinecraftVersion
import net.axay.pacmc.common.data.ModLoader
import net.axay.pacmc.common.data.Repository
import net.axay.pacmc.repoapi.CachePolicy
import net.axay.pacmc.repoapi.RequestContext
import net.axay.pacmc.repoapi.curseforge.CurseforgeApi
import net.axay.pacmc.repoapi.modrinth.ModrinthApi
import net.axay.pacmc.repoapi.mojang.LauncherMetaApi
import net.axay.pacmc.repoapi.mojang.model.VersionManifest
import kotlin.time.Duration.Companion.days

// TODO refactor this class with context receivers once they are available

inline fun <R> repoApiContext(
    cachePolicy: CachePolicy = CachePolicy.CACHED_OR_FRESH,
    block: RepositoryApi.(context: RequestContext) -> R,
): R {
    return block(RepositoryApi, RequestContext(cachePolicy))
}

object RepositoryApi {
    private val cache = MemoryDiskCache<String, String, String>(
        diskConfig = DiskCacheConfig(
            Environment.fileSystem,
            Environment.cacheDir.resolve("requests"),
            keyToFileName = {
                okio.Buffer().writeUtf8(it).sha256().hex()
            },
            deserializer = { readUtf8() },
            serializer = { writeUtf8(it) },
        ),
        validationConfig = CacheValidationConfig(
            expireAfterWrite = 7.days,
            expireAfterAccess = null,
        )
    )

    private val modrinthApi = ModrinthApi(ktorClient, ktorClientJson, cache)
    private val curseforgeApi = CurseforgeApi(ktorClient, ktorClientJson, cache)
    private val launcherMetaApi = LauncherMetaApi(ktorClient, ktorClientJson, cache)

    suspend fun RequestContext.search(searchTerm: String, repository: Repository?): List<CommonProjectResult> {
        val results = mutableListOf<CommonProjectResult>()

        if (repository == null || repository == Repository.MODRINTH) {
            results += with(modrinthApi) { searchProjects(searchTerm, limit = 8) }?.hits.orEmpty()
                .map(CommonProjectResult.Companion::fromModrinthProjectResult)
        }

        if (repository == null || repository == Repository.CURSEFORGE) {
            results += with(curseforgeApi) { searchProjects(searchTerm, pageSize = 8) }?.data.orEmpty()
                .map(CommonProjectResult.Companion::fromCurseforgeMod)
        }

        return results
    }

    suspend fun RequestContext.getProject(idOrSlug: IdOrSlug): CommonProject? = when (idOrSlug.repository) {
        Repository.MODRINTH -> with(modrinthApi) { getProject(idOrSlug.idOrSlug) }?.let(CommonProject::fromModrinthProject)
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun RequestContext.getBasicProjectInfo(idOrSlug: IdOrSlug): CommonBasicProject? = when (idOrSlug.repository) {
        Repository.MODRINTH -> with(modrinthApi) { getProject(idOrSlug.idOrSlug) }?.let(CommonBasicProject::fromModrinthProject)
        Repository.CURSEFORGE -> with(curseforgeApi) { getProject(idOrSlug) }?.let(CommonBasicProject::fromCurseforgeMod)
    }

    suspend fun RequestContext.getProjectVersion(
        projectIdOrSlug: IdOrSlug,
        versionId: String,
    ): CommonProjectVersion? = when (projectIdOrSlug.repository) {
        Repository.MODRINTH -> with(modrinthApi) { getProjectVersion(versionId) }?.let(CommonProjectVersion::fromModrinthProjectVersion)
        Repository.CURSEFORGE -> with (curseforgeApi) { getProjectVersion(projectIdOrSlug, versionId) }?.let(CommonProjectVersion::fromCurseforgeFile)
    }

    suspend fun RequestContext.getProjectVersions(
        idOrSlug: IdOrSlug,
        loaders: List<ModLoader>? = null,
    ) = when (idOrSlug.repository) {
        Repository.MODRINTH -> {
            with(modrinthApi) { getProjectVersions(idOrSlug.idOrSlug, loaders?.map { it.identifier }) }
                ?.map(CommonProjectVersion::fromModrinthProjectVersion)
        }
        Repository.CURSEFORGE -> with(curseforgeApi) { getProjectVersions(idOrSlug, loaders) }
            ?.map(CommonProjectVersion::fromCurseforgeFile)
    }

    suspend fun RequestContext.getMinecraftReleases(): List<MinecraftVersion>? {
        val manifest = with(launcherMetaApi) { getVersionManifest() } ?: return null
        return manifest.versions.filter { it.type == VersionManifest.Version.Type.RELEASE }
            .mapNotNull { MinecraftVersion.fromString(it.id) }
    }
}
