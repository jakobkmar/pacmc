package net.axay.pacmc.repoapi

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

abstract class AbstractRepositoryApi {
    protected abstract val apiUrl: String
    protected abstract val client: HttpClient

    protected suspend inline fun <reified T> repoRequest(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T? {
        val response: HttpResponse = client.request("${apiUrl}${url}") {
            method = HttpMethod.Get
            block()
        }
        return if (response.status.isSuccess()) {
            response.receive()
        } else {
            null
        }
    }
}
