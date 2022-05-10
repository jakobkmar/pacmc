package net.axay.pacmc.repoapi.curseforge

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache
import net.axay.pacmc.repoapi.AbstractRepositoryApi
import net.axay.pacmc.repoapi.RequestContext
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
    ) = repoRequest<SearchModsResponse>("/mods/search") {
        parameter("gameId", 432) // TODO request this value
        parameter("searchFilter", searchFilter)
        parameter("pageSize", pageSize)
    }
}
