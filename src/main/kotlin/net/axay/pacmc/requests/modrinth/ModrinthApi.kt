package net.axay.pacmc.requests.modrinth

import io.ktor.client.request.*
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.modrinth.data.ModrinthSearchResponse

object ModrinthApi {
    private const val apiUrl = "https://api.modrinth.com/api/v1/"

    suspend fun search(query: String) =
        ktorClient.get<ModrinthSearchResponse>("${apiUrl}mod") {
            parameter("query", query)
        }
}
