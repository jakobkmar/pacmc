package net.axay.pacmc.app.repoapi

import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.app.repoapi.model.CommonProjectInfo
import net.axay.pacmc.repoapi.modrinth.ModrinthApi

object RepositoryApi {
    private val modrinthApi = ModrinthApi(client = ktorClient)

    suspend fun search(searchTerm: String, repository: Repository?): List<CommonProjectInfo> {
        val results = mutableListOf<CommonProjectInfo>()

        if (repository == null || repository == Repository.MODRINTH) {
            results += modrinthApi.searchProjects(searchTerm)?.hits.orEmpty().map {
                CommonProjectInfo.fromModrinthProjectResult(it)
            }
        }

        if (repository == null || repository == Repository.CURSEFORGE) {
            TODO()
        }

        return results
    }

    suspend fun getProject(modId: ModId): CommonProjectInfo? {
        return when (modId.repository) {
            Repository.MODRINTH -> TODO()
            Repository.CURSEFORGE -> TODO()
        }
    }
}
