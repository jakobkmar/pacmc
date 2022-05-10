package net.axay.pacmc.repoapi.curseforge

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache
import net.axay.pacmc.common.data.IdOrSlug
import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.repoapi.AbstractRepositoryApi
import net.axay.pacmc.repoapi.RequestContext
import net.axay.pacmc.repoapi.curseforge.model.Mod
import net.axay.pacmc.repoapi.curseforge.model.ModsSearchSortField
import net.axay.pacmc.repoapi.curseforge.model.SearchModsResponse

class CurseforgeApi(
    override val client: HttpClient,
    override val clientJson: Json,
    override val cache: Cache<String, String, String>?,
    override val apiUrl: String = "https://api.curseforge.com/v1",
) : AbstractRepositoryApi() {

    override val headers = StringValues.build {
        append("x-api-key", "\$2a\$10\$NVywddNPLdJ93qG4QFp/fOjGIcM9323G3L0VDDKTEtGwS0MEnDhTO")
    }

    suspend fun RequestContext.searchProjects(
        searchFilter: String,
        pageSize: Int? = null,
        sortOrder: ModsSearchSortField? = null,
    ) = repoRequest<SearchModsResponse>("/mods/search") {
        parameter("gameId", 432) // TODO request this value
        parameter("searchFilter", searchFilter)
        parameter("pageSize", pageSize)
        parameter("sortOrder", sortOrder?.ordinal?.plus(1))
    }

    suspend fun RequestContext.getProject(
        idOrSlug: IdOrSlug,
    ): Mod? {
        val id = when (idOrSlug) {
            is ModId -> idOrSlug.id
            is ModSlug -> searchProjects(idOrSlug.slug, sortOrder = ModsSearchSortField.NAME)
                ?.data?.find { it.slug == idOrSlug.slug }?.id?.toString()
        } ?: return null

        return repoRequest<Mod>("/mods/$id")
    }
}
