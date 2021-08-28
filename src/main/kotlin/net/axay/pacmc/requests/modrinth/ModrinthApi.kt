package net.axay.pacmc.requests.modrinth

import io.ktor.client.request.*
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.modrinth.data.*

object ModrinthApi {
    private const val apiUrl = "https://api.modrinth.com/api/v1/"

    suspend fun search(query: String, limit: Int?) =
        ktorClient.get<ModrinthSearchResponse>("${apiUrl}mod") {
            parameter("query", query)
            if (limit != null)
                parameter("limit", limit)
        }

    suspend fun getModVersions(id: String) = kotlin.runCatching {
        ktorClient.get<List<ModrinthModVersion>>("${apiUrl}mod/$id/version")
    }.getOrNull()

    suspend fun getModVersion(id: String) = kotlin.runCatching {
        ktorClient.get<ModrinthModVersion>("${apiUrl}version/$id")
    }.getOrNull()

    suspend fun getModInfo(id: String) = kotlin.runCatching {
        ktorClient.get<ModrinthModInfo>("${apiUrl}mod/$id")
    }.getOrNull()

    suspend fun getTeamMembers(id: String) = kotlin.runCatching {
        ktorClient.get<List<ModrinthTeamMember>>("${apiUrl}team/$id/members")
    }.getOrNull()

    suspend fun getUser(id: String) = kotlin.runCatching {
        ktorClient.get<ModrinthUser>("${apiUrl}user/$id")
    }.getOrNull()

    suspend fun getModDescriptionBody(id: String) = kotlin.runCatching {
        ktorClient.get<ModrinthModDescriptionBody>("${apiUrl}mod/$id")
    }.getOrNull()
}
