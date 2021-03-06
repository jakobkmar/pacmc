package net.axay.pacmc.requests.curse

import io.ktor.client.request.*
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.curse.data.CurseProxyFile
import net.axay.pacmc.requests.curse.data.CurseProxyMinecraftVersion
import net.axay.pacmc.requests.curse.data.CurseProxyProject
import net.axay.pacmc.requests.curse.data.CurseProxyProjectInfo
import net.axay.pacmc.utils.repositoryRequest
import net.axay.pacmc.utils.similarity

object CurseProxy {
    private const val proxyApi = "https://addons-ecs.forgesvc.net/api/v2/"

    suspend fun getMinecraftVersions() =
        ktorClient.get<List<CurseProxyMinecraftVersion>>("${proxyApi}minecraft/version")

    suspend fun search(searchTerm: String, gameVersion: String?, limit: Int?) =
        ktorClient.repositoryRequest<List<CurseProxyProject>>("${proxyApi}addon/search") {
            parameter("gameId", 432) // game: minecraft
            parameter("sectionId", 6) // section: mods
            parameter("searchFilter", searchTerm)
            if (gameVersion != null) parameter("gameVersion", gameVersion)
        }
        ?.filter { it.modLoaders?.map { modLoaderName -> modLoaderName.lowercase() }?.contains("fabric") != false }
        ?.sortedWith { mod1, mod2 ->
            val mod1Similarity = mod1.name.similarity(searchTerm)
            val mod2Similarity = mod2.name.similarity(searchTerm)
            when {
                mod1.slug == searchTerm -> 3
                mod2.slug == searchTerm -> -3
                mod1Similarity == 1.0 -> 2
                mod2Similarity == 1.0 -> -2
                mod1.gamePopularityRank < 120 && mod1.gamePopularityRank < mod2.gamePopularityRank -> 1
                mod2.gamePopularityRank < 120 && mod2.gamePopularityRank < mod1.gamePopularityRank -> -1
                else -> mod1Similarity.compareTo(mod2Similarity)
                    .let { if (it == 0) mod1.gamePopularityRank.compareTo(mod2.gamePopularityRank) else it }
            }.inv()
        }
        ?.let { if (limit != null) it.take(limit) else it }

    suspend fun getModFiles(id: Int) =
        ktorClient.repositoryRequest<List<CurseProxyFile>>("${proxyApi}addon/$id/files")

    suspend fun getModInfo(id: Int) =
        ktorClient.repositoryRequest<CurseProxyProjectInfo>("${proxyApi}addon/$id")
}
