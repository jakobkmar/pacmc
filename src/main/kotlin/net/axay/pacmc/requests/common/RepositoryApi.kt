package net.axay.pacmc.requests.common

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.axay.pacmc.data.Repository
import net.axay.pacmc.requests.common.data.CommonModInfo
import net.axay.pacmc.requests.common.data.CommonModResult
import net.axay.pacmc.requests.common.data.CommonModVersion
import net.axay.pacmc.requests.curse.CurseProxy
import net.axay.pacmc.requests.modrinth.ModrinthApi

object RepositoryApi {
    suspend fun search(
        query: String,
        mainRepoLimit: Int,
        otherRepoLimit: Int,
        generalLimit: Int = mainRepoLimit + otherRepoLimit,
    ): List<CommonModResult> = coroutineScope {
        val results = ArrayList<CommonModResult>()

        // TODO: allow filtering for game version
        val modrinthResults = async {
            ModrinthApi.search(query, mainRepoLimit).hits
        }
        val curseforgeResults = async {
            CurseProxy.search(query, null, otherRepoLimit)
        }

        // add all modrinth results
        modrinthResults.await().forEach { results += it.convertToCommon() }

        // filter the curseforge results
        curseforgeResults.await()
            .map { it.convertToCommon() }
            .forEach { curseforgeResult ->
                if (results.size >= generalLimit) return@forEach

                val alreadyPresent = results.any { presentResult ->
                    // only if this is a different repo this can be duplicated mod
                    if (presentResult.repository != curseforgeResult.repository) {
                        // consider mods with the same name or slug to be a possible duplicate
                        val sameNameOrSlug = presentResult.slug.contentEquals(curseforgeResult.slug, true) ||
                                presentResult.name.contentEquals(curseforgeResult.name, true)

                        if (sameNameOrSlug) {
                            // check the author OR the description, if one them is the same this is a possible duplicate
                            val sameAuthorOrDescription = presentResult.author.contentEquals(curseforgeResult.author, true) ||
                                    presentResult.description.contentEquals(curseforgeResult.description, true)

                            if (sameAuthorOrDescription) return@any true // found a duplicate
                        }
                    }
                    false // no duplicate
                }
                if (!alreadyPresent) results += curseforgeResult
            }

        results
    }

    suspend fun getModVersions(id: String): List<CommonModVersion>? {
        return when {
            id.any { it.isLetter() } -> ModrinthApi.getModVersions(id)
            else -> CurseProxy.getModFiles(id.toInt())
        }?.map { it.convertToCommon() }
    }

    // TODO: warning for curseforge
    suspend fun getModVersion(id: String) =
        ModrinthApi.getModVersion(id)?.convertToCommon()

    suspend fun getModInfo(id: String, repository: Repository? = null): CommonModInfo? {
        return when (repository ?: when {
            id.any { it.isLetter() } -> Repository.MODRINTH
            else -> Repository.CURSEFORGE
        }) {
            Repository.MODRINTH -> ModrinthApi.getModInfo(id)?.convertToCommon()
            Repository.CURSEFORGE -> CurseProxy.getModInfo(id.toInt())?.convertToCommon()
        }
    }
}
