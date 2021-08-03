package net.axay.pacmc.requests.common

import net.axay.pacmc.data.Repository
import net.axay.pacmc.requests.common.data.CommonModResult
import net.axay.pacmc.requests.common.data.CommonModVersion
import net.axay.pacmc.requests.curse.CurseProxy
import net.axay.pacmc.requests.modrinth.ModrinthApi

object RepositoryApi {
    suspend fun search(query: String): ArrayList<CommonModResult> {
        val results = ArrayList<CommonModResult>()

        ModrinthApi.search(query).hits.forEach { results += it.convertToCommon() }

        CurseProxy.search(query, null, null)
            .map { it.convertToCommon() }
            .forEach { curseforgeResult ->
                val alreadyPresent = results.any { presentResult ->
                    presentResult.repository != curseforgeResult.repository &&
                            presentResult.name.contentEquals(curseforgeResult.name, true)
                }
                if (!alreadyPresent)
                    results += curseforgeResult
            }

        return results
    }

    suspend fun getModFiles(repository: Repository, id: String): List<CommonModVersion>? {
        return when (repository) {
            Repository.MODRINTH -> ModrinthApi.getModVersions(id)
            Repository.CURSEFORGE -> CurseProxy.getModFiles(id.toInt())
        }?.map { it.convertToCommon() }
    }
}
