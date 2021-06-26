package net.axay.pacmc.requests

import io.ktor.client.features.*
import io.ktor.client.request.*
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.data.CurseProxyFile
import net.axay.pacmc.requests.data.CurseProxyMinecraftVersion
import net.axay.pacmc.requests.data.CurseProxyProject
import net.axay.pacmc.requests.data.CurseProxyProjectInfo
import net.axay.pacmc.utils.similarity

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
        .sortedWith { mod1, mod2 ->
            val mod1Similarity = mod1.name.similarity(searchTerm)
            val mod2Similarity = mod2.name.similarity(searchTerm)
            when {
                mod1Similarity == 1.0 -> 2
                mod2Similarity == 1.0 -> -2
                mod1.gamePopularityRank < 120 && mod1.gamePopularityRank < mod2.gamePopularityRank -> 1
                mod2.gamePopularityRank < 120 && mod2.gamePopularityRank < mod1.gamePopularityRank -> -1
                else -> mod1Similarity.compareTo(mod2Similarity)
                    .let { if (it == 0) mod1.gamePopularityRank.compareTo(mod2.gamePopularityRank) else it }
            }.inv()
        }

    suspend fun getModFiles(id: Int) = try {
        ktorClient.get<List<CurseProxyFile>>("${proxyApi}addon/$id/files")
    } catch (exc: ClientRequestException) {
        null
    }

    suspend fun getModInfo(id: Int) = ktorClient.get<CurseProxyProjectInfo>("${proxyApi}addon/$id")
}
