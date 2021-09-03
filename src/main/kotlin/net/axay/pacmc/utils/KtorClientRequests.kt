package net.axay.pacmc.utils

import com.github.ajalt.mordant.rendering.TextStyles
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import net.axay.pacmc.terminal

suspend inline fun <reified T> HttpClient.repositoryRequest(urlString: String): T? {
    val response: HttpResponse = get(urlString)
    val status = response.status

    if (status.isSuccess())
        return try {
            response.receive<T>()
        } catch (exception: SerializationException) {
            terminal.println()
            terminal.danger("Failed to deserialize the response to the following request: $urlString")
            terminal.println("${TextStyles.underline(TextStyles.bold("Error message:"))} ${exception.message}")
            terminal.println()
            null
        }

    when (status) {
        HttpStatusCode.NotFound -> return null
        HttpStatusCode.Unauthorized -> terminal.warning("Did an unauthorized request (code ${status.value}) to the following url: $urlString")
        else -> terminal.warning("Got a ${status.description} answer (code ${status.value}) for the following request: $urlString")
    }

    return null
}
