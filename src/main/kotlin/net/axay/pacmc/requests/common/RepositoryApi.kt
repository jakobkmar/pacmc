package net.axay.pacmc.requests.common

import kotlinx.coroutines.*
import net.axay.pacmc.data.Repository
import net.axay.pacmc.requests.common.data.CommonModInfo
import net.axay.pacmc.requests.common.data.CommonModResult
import net.axay.pacmc.requests.common.data.CommonModVersion
import net.axay.pacmc.requests.curse.CurseProxy
import net.axay.pacmc.requests.modrinth.ModrinthApi
import net.axay.pacmc.terminal

object RepositoryApi {
    suspend fun search(
        query: String,
        mainRepoLimit: Int,
        otherRepoLimit: Int,
        generalLimit: Int = mainRepoLimit + otherRepoLimit,
        showWaitingMessage: Boolean = false,
    ): List<CommonModResult> = coroutineScope {
        val results = ArrayList<CommonModResult>()

        // TODO: allow filtering for game version
        val modrinthResults = async {
            ModrinthApi.search(query, mainRepoLimit).hits
        }
        val curseforgeResults = async {
            CurseProxy.search(query, null, otherRepoLimit)
        }

        val waitingMessageJob = launch(start = CoroutineStart.LAZY) {
            while (isActive) {
                delay(1000L * 4)
                if (!isActive) break

                if (!modrinthResults.isCompleted)
                    terminal.println("Still waiting for Modrinth to answer...")
                if (!curseforgeResults.isCompleted)
                    terminal.println("Still waiting for Curseforge to answer...")

                delay(1000L * 30)
            }
        }
        if (showWaitingMessage) waitingMessageJob.start()

        // add all modrinth results
        modrinthResults.await().forEach { results += it.convertToCommon() }

        // filter the curseforge results
        curseforgeResults.await()
            .forEach { uncommonResult ->
                if (results.size >= generalLimit) return@forEach

                val curseforgeResult = uncommonResult.convertToCommon()

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

        waitingMessageJob.cancel()

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
