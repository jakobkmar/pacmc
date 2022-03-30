package net.axay.pacmc.app.repoapi

import net.axay.pacmc.app.data.IdOrSlug
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.app.repoapi.model.CommonBasicProjectInfo
import net.axay.pacmc.app.repoapi.model.CommonProjectInfo
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.repoapi.modrinth.ModrinthApi
import net.axay.pacmc.repoapi.mojang.LauncherMetaApi
import net.axay.pacmc.repoapi.mojang.model.VersionManifest

object RepositoryApi {
    private val modrinthApi = ModrinthApi(ktorClient)
    private val launcherMetaApi = LauncherMetaApi(ktorClient)

    suspend fun search(searchTerm: String, repository: Repository?): List<CommonProjectInfo> {
        val results = mutableListOf<CommonProjectInfo>()

        if (repository == null || repository == Repository.MODRINTH) {
            results += modrinthApi.searchProjects(searchTerm, limit = 20)?.hits.orEmpty().map {
                CommonProjectInfo.fromModrinthProjectResult(it)
            }
        }

        if (repository == null || repository == Repository.CURSEFORGE) {
            // TODO
        }

        return results
    }

    suspend fun getProject(idOrSlug: IdOrSlug): CommonProjectInfo? = when (idOrSlug.repository) {
        Repository.MODRINTH -> TODO()
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun getBasicProjectInfo(idOrSlug: IdOrSlug): CommonBasicProjectInfo? = when (idOrSlug.repository) {
        Repository.MODRINTH -> modrinthApi.getProject(idOrSlug.idOrSlug)?.let(CommonBasicProjectInfo::fromModrinthProject)
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun getProjectVersion(id: String, repository: Repository): CommonProjectVersion? = when (repository) {
        Repository.MODRINTH -> modrinthApi.getProjectVersion(id)?.let(CommonProjectVersion::fromModrinthProjectVersion)
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun getProjectVersions(
        idOrSlug: IdOrSlug,
        loaders: List<ModLoader>? = null,
        gameVersions: List<MinecraftVersion>? = null,
    ) = when (idOrSlug.repository) {
        Repository.MODRINTH -> {
            modrinthApi.getProjectVersions(idOrSlug.idOrSlug, loaders?.map { it.identifier }, gameVersions?.map { it.toString() })
                ?.map { CommonProjectVersion.fromModrinthProjectVersion(it) }
        }
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun getMinecraftReleases(): List<MinecraftVersion>? {
        val manifest = launcherMetaApi.getVersionManifest() ?: return null
        return manifest.versions.filter { it.type == VersionManifest.Version.Type.RELEASE }
            .mapNotNull { MinecraftVersion.fromString(it.id) }
    }
}
