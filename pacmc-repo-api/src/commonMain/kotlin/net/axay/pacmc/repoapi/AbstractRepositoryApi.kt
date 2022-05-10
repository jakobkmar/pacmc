package net.axay.pacmc.repoapi

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache

abstract class AbstractRepositoryApi {
    protected abstract val apiUrl: String
    protected abstract val client: HttpClient
    protected abstract val cache: Cache<String, String, String>?
    protected abstract val clientJson: Json

    protected open val headers: StringValues? = null

    protected suspend inline fun <reified T> RequestContext.repoRequest(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): T? {
        var finalUrl = ""
        val statement = client.prepareRequest("${apiUrl}${url}") {
            method = HttpMethod.Get
            this@AbstractRepositoryApi.headers?.let { headers.appendAll(it) }
            block()
            finalUrl = this.url.buildString()
        }
        require(finalUrl.isNotEmpty())

        val curCache = cache

        val responseString = when (cachePolicy) {
            CachePolicy.ONLY_CACHED -> curCache?.get(finalUrl)
            CachePolicy.ONLY_FRESH -> {
                // TODO already lock resolving of the url for this cache
                val response = statement.executeOrNull()?.bodyAsText()
                if (response != null) {
                    cache?.put(finalUrl, response)
                }
                response
            }
            CachePolicy.CACHED_OR_FRESH -> {
                if (curCache != null) {
                    curCache.getOrPutOrNull(finalUrl) {
                        statement.executeOrNull()?.bodyAsText()
                    }
                } else {
                    statement.executeOrNull()?.bodyAsText()
                }
            }
        }

        return clientJson.decodeFromString(responseString ?: return null)
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
