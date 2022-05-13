package net.axay.pacmc.repoapi.curseforge

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache
import net.axay.pacmc.common.data.IdOrSlug
import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModLoader
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.repoapi.AbstractRepositoryApi
import net.axay.pacmc.repoapi.RequestContext
import net.axay.pacmc.repoapi.curseforge.model.*

class CurseforgeApi(
    override val client: HttpClient,
    override val clientJson: Json,
    override val cache: Cache<String, String, String>?,
    override val apiUrl: String = "https://api.curseforge.com/v1",
) : AbstractRepositoryApi() {

    override val headers = StringValues.build {
        append("x-api-key", "\$2a\$10\$NVywddNPLdJ93qG4QFp/fOjGIcM9323G3L0VDDKTEtGwS0MEnDhTO")
    }

    private suspend fun RequestContext.resolveId(idOrSlug: IdOrSlug): String? {
        return when (idOrSlug) {
            is ModId -> idOrSlug.id
            is ModSlug -> searchProjects(idOrSlug.slug, isSlug = true)
                ?.find { it.slug == idOrSlug.slug }?.id?.toString()
        }
    }

    suspend fun RequestContext.searchProjects(
        searchFilter: String,
        pageSize: Int? = null,
        sortOrder: ModsSearchSortField? = null,
        isSlug: Boolean = false,
    ): List<Mod>? = repoRequest<CurseforgeDataWrapper<List<Mod>>>("/mods/search") {
        parameter("gameId", 432) // TODO request this value
        parameter(if (isSlug) "slug" else "searchFilter", searchFilter)
        parameter("pageSize", pageSize)
        parameter("sortOrder", sortOrder?.ordinal?.plus(1))
    }?.data

    suspend fun RequestContext.getProject(idOrSlug: IdOrSlug): Mod? {
        return repoRequest<CurseforgeDataWrapper<Mod>>("/mods/${resolveId(idOrSlug) ?: return null}")?.data
    }

    suspend fun RequestContext.getProjectVersion(projectIdOrSlug: IdOrSlug, versionId: String): File? {
        return repoRequest<CurseforgeDataWrapper<File>>("/mods/${resolveId(projectIdOrSlug) ?: return null}/files/${versionId}")?.data
    }

    suspend fun RequestContext.getProjectVersions(
        idOrSlug: IdOrSlug,
        loaders: List<ModLoader>? = null,
    ): List<File>? {
        return if (loaders == null || loaders.isEmpty())
            repoRequest<CurseforgeDataWrapper<List<File>>>("/mods/${resolveId(idOrSlug) ?: return null}/files")?.data
        else coroutineScope {
            loaders.map { loader ->
                async {
                    repoRequest<CurseforgeDataWrapper<List<File>>>("/mods/${resolveId(idOrSlug) ?: return@async null}/files") {
                        parameter("modLoaderType", loader.curseforgeId ?: error("The loader ${loader.displayName} is not supported by Curseforge"))
                    }?.data
                }
            }.awaitAll().filterNotNull().flatten().distinctBy { it.id }
        }
    }
}
