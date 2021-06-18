package net.axay.pacmc.requests

import io.ktor.client.features.*
import io.ktor.client.request.*
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.data.CurseProxyFile
import net.axay.pacmc.requests.data.CurseProxyMinecraftVersion
import net.axay.pacmc.requests.data.CurseProxyProject

object CurseProxy {
    private const val proxyApi = "https://addons-ecs.forgesvc.net/api/v2/"

    suspend fun getMinecraftVersions() =
        ktorClient.get<List<CurseProxyMinecraftVersion>>("${proxyApi}minecraft/version")

    suspend fun search(searchTerm: String, gameVersion: String?, limit: Int?) =
        ktorClient.get<List<CurseProxyProject>>("${proxyApi}addon/search") {
            parameter("gameId", 432) // game: minecraft
            parameter("sectionId", 6) // section: mods
            parameter("searchFilter", searchTerm)
            parameter("categoryId", 4780) // category: fabric
            if (gameVersion != null) parameter("gameVersion", gameVersion)
            if (limit != null) parameter("pageSize", limit)
        }

    suspend fun getModFiles(id: Int) = try {
        ktorClient.get<List<CurseProxyFile>>("${proxyApi}addon/$id/files")
    } catch (exc: ClientRequestException) {
        null
    }
}
