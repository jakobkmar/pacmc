package net.axay.pacmc.repoapi.modrinth

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache
import net.axay.pacmc.repoapi.AbstractRepositoryApi
import net.axay.pacmc.repoapi.RequestContext
import net.axay.pacmc.repoapi.modrinth.model.*

class ModrinthApi(
    override val client: HttpClient,
    override val clientJson: Json,
    override val cache: Cache<String, String, String>?,
    override val apiUrl: String = "https://api.modrinth.com/v2",
) : AbstractRepositoryApi() {

    private fun List<String>.joinQuotedStrings() =
        joinToString("\", ", "[\"", "\"]")

    suspend fun RequestContext.searchProjects(
        query: String,
        facets: String? = null,
        index: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        filters: String? = null,
    ) = repoRequest<SearchResults>("/search") {
        parameter("query", query)
        parameter("facets", facets)
        parameter("index", index)
        parameter("offset", offset)
        parameter("limit", limit)
        parameter("filters", filters)
    }

    suspend fun RequestContext.getProject(idOrSlug: String) =
        repoRequest<Project>("/project/${idOrSlug}")

    suspend fun RequestContext.getProjectVersions(
        idOrSlug: String,
        loaders: List<String>? = null,
        gameVersions: List<String>? = null,
        featured: Boolean? = null,
    ) = repoRequest<List<Version>>("/project/${idOrSlug}/version") {
        parameter("loaders", loaders?.joinQuotedStrings())
        parameter("game_versions", gameVersions?.joinQuotedStrings())
        parameter("featured", featured)
    }

    suspend fun RequestContext.getProjectVersion(id: String) =
        repoRequest<Version>("/version/${id}")

    suspend fun RequestContext.getUser(idOrUsername: String) =
        repoRequest<User>("/user/${idOrUsername}")

    suspend fun RequestContext.getUserProjects(idOrUsername: String) =
        repoRequest<List<Project>>("/user/${idOrUsername}/projects")

    suspend fun RequestContext.getTeamMembers(id: String) =
        repoRequest<List<TeamMember>>("/team/${id}/members")

    suspend fun RequestContext.getProjectTeamMembers(idOrSlug: String) =
        repoRequest<List<TeamMember>>("/project/${idOrSlug}/members")
}
