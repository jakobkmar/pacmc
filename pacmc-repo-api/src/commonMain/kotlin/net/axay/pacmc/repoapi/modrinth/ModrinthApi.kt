package net.axay.pacmc.repoapi.modrinth

import io.ktor.client.*
import io.ktor.client.request.*
import net.axay.pacmc.repoapi.RepositoryApi
import net.axay.pacmc.repoapi.modrinth.model.*

class ModrinthApi(
    override val client: HttpClient,
) : RepositoryApi("https://staging-api.modrinth.com/v2") {

    suspend fun searchProjects(
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

    suspend fun getProject(idOrSlug: String) =
        repoRequest<Project>("/project/${idOrSlug}")

    suspend fun getProjectVersions(idOrSlug: String) =
        repoRequest<List<Version>>("/project/${idOrSlug}/version")

    suspend fun getProjectVersion(id: String) =
        repoRequest<Version>("/version/${id}")

    suspend fun getUser(idOrUsername: String) =
        repoRequest<User>("/user/${idOrUsername}")

    suspend fun getUserProjects(idOrUsername: String) =
        repoRequest<List<Project>>("/user/${idOrUsername}/projects")

    suspend fun getTeamMembers(id: String) =
        repoRequest<List<TeamMember>>("/team/${id}/members")

    suspend fun getProjectTeamMembers(idOrSlug: String) =
        repoRequest<List<TeamMember>>("/project/${idOrSlug}/members")
}
