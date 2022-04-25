package net.axay.pacmc.app.repoapi

import net.axay.memoire.CacheValidationConfig
import net.axay.memoire.DiskCacheConfig
import net.axay.memoire.MemoryDiskCache
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.data.IdOrSlug
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.app.repoapi.model.CommonBasicProjectInfo
import net.axay.pacmc.app.repoapi.model.CommonProjectInfo
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.repoapi.CachePolicy
import net.axay.pacmc.repoapi.RequestContext
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

    private val modrinthApi = ModrinthApi(ktorClient, cache)
    private val launcherMetaApi = LauncherMetaApi(ktorClient, cache)

    suspend fun RequestContext.search(searchTerm: String, repository: Repository?): List<CommonProjectInfo> {
        val results = mutableListOf<CommonProjectInfo>()

        if (repository == null || repository == Repository.MODRINTH) {
            results += with(modrinthApi) { searchProjects(searchTerm, limit = 20) }?.hits.orEmpty().map {
                CommonProjectInfo.fromModrinthProjectResult(it)
            }
        }

        if (repository == null || repository == Repository.CURSEFORGE) {
            // TODO
        }

        return results
    }

    suspend fun RequestContext.getProject(idOrSlug: IdOrSlug): CommonProjectInfo? = when (idOrSlug.repository) {
        Repository.MODRINTH -> TODO()
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun RequestContext.getBasicProjectInfo(idOrSlug: IdOrSlug): CommonBasicProjectInfo? = when (idOrSlug.repository) {
        Repository.MODRINTH -> with(modrinthApi) { getProject(idOrSlug.idOrSlug) }?.let(CommonBasicProjectInfo::fromModrinthProject)
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun RequestContext.getProjectVersion(id: String, repository: Repository): CommonProjectVersion? = when (repository) {
        Repository.MODRINTH -> with(modrinthApi) { getProjectVersion(id) }?.let(CommonProjectVersion::fromModrinthProjectVersion)
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun RequestContext.getProjectVersions(
        idOrSlug: IdOrSlug,
        loaders: List<ModLoader>? = null,
        gameVersions: List<MinecraftVersion>? = null,
    ) = when (idOrSlug.repository) {
        Repository.MODRINTH -> {
            with(modrinthApi) { getProjectVersions(idOrSlug.idOrSlug, loaders?.map { it.identifier }, gameVersions?.map { it.toString() }) }
                ?.map { CommonProjectVersion.fromModrinthProjectVersion(it) }
        }
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun RequestContext.getMinecraftReleases(): List<MinecraftVersion>? {
        val manifest = with(launcherMetaApi) { getVersionManifest() } ?: return null
        return manifest.versions.filter { it.type == VersionManifest.Version.Type.RELEASE }
            .mapNotNull { MinecraftVersion.fromString(it.id) }
    }
}
