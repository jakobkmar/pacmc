package net.axay.pacmc.repoapi

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache

abstract class AbstractRepositoryApi {
    protected abstract val apiUrl: String
    protected abstract val client: HttpClient
    protected abstract val cache: Cache<String, String, String>?

    protected suspend inline fun <reified T> repoRequest(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): T? {
        var finalUrl = ""
        val statement = client.prepareRequest("${apiUrl}${url}") {
            method = HttpMethod.Get
            block()
            finalUrl = this.url.buildString()
        }
        require(finalUrl.isNotEmpty())

        val responseString = cache?.let {
            it.getOrPutOrNull(finalUrl.removePrefix("${apiUrl}/")) {
                statement.executeOrNull()?.bodyAsText()
            } ?: return null
        } ?: statement.executeOrNull()?.bodyAsText()

        return Json.decodeFromString(responseString ?: return null)
    }

    protected suspend fun HttpStatement.executeOrNull(): HttpResponse? {
        val response = execute()
        return if (response.status.isSuccess()) {
            response
        } else {
            null
        }
    }
}
