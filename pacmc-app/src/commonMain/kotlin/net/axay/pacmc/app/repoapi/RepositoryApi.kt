package net.axay.pacmc.app.repoapi

import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.app.repoapi.model.CommonProjectInfo
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.repoapi.modrinth.ModrinthApi

object RepositoryApi {
    private val modrinthApi = ModrinthApi(client = ktorClient)

    suspend fun search(searchTerm: String, repository: Repository?): List<CommonProjectInfo> {
        val results = mutableListOf<CommonProjectInfo>()

        if (repository == null || repository == Repository.MODRINTH) {
            results += modrinthApi.searchProjects(searchTerm, limit = 20)?.hits.orEmpty().map {
                CommonProjectInfo.fromModrinthProjectResult(it)
            }
        }

        if (repository == null || repository == Repository.CURSEFORGE) {
            TODO()
        }

        return results
    }

    suspend fun getProject(modId: ModId): CommonProjectInfo? = when (modId.repository) {
        Repository.MODRINTH -> TODO()
        Repository.CURSEFORGE -> TODO()
    }

    suspend fun getProjectVersions(
        modId: ModId,
        loaders: List<ModLoader>? = null,
        gameVersions: List<MinecraftVersion>? = null,
    ) = when (modId.repository) {
        Repository.MODRINTH -> {
            modrinthApi.getProjectVersions(modId.id, loaders?.map { it.identifier }, gameVersions?.map { it.toString() })
                ?.map { CommonProjectVersion.fromModrinthProjectVersion(it) }
        }
        Repository.CURSEFORGE -> TODO()
    }
}
