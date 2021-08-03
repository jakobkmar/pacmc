package net.axay.pacmc.requests.modrinth

import io.ktor.client.features.*
import io.ktor.client.request.*
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.modrinth.data.ModrinthModVersion
import net.axay.pacmc.requests.modrinth.data.ModrinthSearchResponse

object ModrinthApi {
    private const val apiUrl = "https://api.modrinth.com/api/v1/"

    suspend fun search(query: String, limit: Int?) =
        ktorClient.get<ModrinthSearchResponse>("${apiUrl}mod") {
            parameter("query", query)
            if (limit != null)
                parameter("limit", limit)
        }

    suspend fun getModVersions(id: String) =
        try {
            ktorClient.get<List<ModrinthModVersion>>("${apiUrl}mod/$id/version")
        } catch (exc: ClientRequestException) {
            null
        }
}
