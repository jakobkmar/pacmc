package net.axay.pacmc.requests.modrinth

import io.ktor.client.request.*
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.modrinth.data.*
import net.axay.pacmc.utils.repositoryRequest

object ModrinthApi {
    private const val apiUrl = "https://api.modrinth.com/api/v1/"

    suspend fun search(query: String, limit: Int?) =
        ktorClient.get<ModrinthSearchResponse>("${apiUrl}mod") {
            parameter("query", query)
            if (limit != null)
                parameter("limit", limit)
        }

    suspend fun getModVersions(id: String) =
        ktorClient.repositoryRequest<List<ModrinthModVersion>>("${apiUrl}mod/$id/version")

    suspend fun getModVersion(id: String) =
        ktorClient.repositoryRequest<ModrinthModVersion>("${apiUrl}version/$id")

    suspend fun getModInfo(id: String) =
        ktorClient.repositoryRequest<ModrinthModInfo>("${apiUrl}mod/$id")

    suspend fun getTeamMembers(id: String) =
        ktorClient.repositoryRequest<List<ModrinthTeamMember>>("${apiUrl}team/$id/members")

    suspend fun getUser(id: String) =
        ktorClient.repositoryRequest<ModrinthUser>("${apiUrl}user/$id")

    suspend fun getModDescriptionBody(id: String) =
        ktorClient.repositoryRequest<ModrinthModDescriptionBody>("${apiUrl}mod/$id")
}
