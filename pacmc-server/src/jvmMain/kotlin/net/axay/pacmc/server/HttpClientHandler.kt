package net.axay.pacmc.server

import io.ktor.http.*
import kotlinx.coroutines.future.asDeferred
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val httpClient: HttpClient = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.ALWAYS)
    .build()

suspend fun HttpClient.requestText(url: String): Result<HttpResponse<String>> {
    val rssRequest = HttpRequest.newBuilder(URI(url))
        .GET().build()

    return kotlin.runCatching {
        val response = sendAsync(rssRequest, HttpResponse.BodyHandlers.ofString())
            .asDeferred().await()

        if (!HttpStatusCode.fromValue(response.statusCode()).isSuccess())
            error("Got invalid status code (${response.statusCode()}) for http request to $url")

        response
    }
}
