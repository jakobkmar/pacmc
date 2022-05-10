package net.axay.pacmc.app

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import okio.Path

val ktorClientJson = Json {
    ignoreUnknownKeys = true
}

val ktorClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(ktorClientJson)
    }
}

suspend inline fun HttpClient.downloadFile(
    url: String,
    path: Path,
    noinline downloadProgress: (suspend (Double) -> Unit)? = null,
    builder: HttpRequestBuilder.() -> Unit = {},
) = prepareRequest(url) {
    builder(this)

    if (downloadProgress != null) {
        onDownload { bytesSentTotal, contentLength ->
            val progress = bytesSentTotal.toDouble() / contentLength.toDouble()
            if (!progress.isNaN()) {
                downloadProgress(progress)
            }
        }
    }
}.execute { response ->
    val channel = response.bodyAsChannel()

    val partPath = path.parent!!.resolve(path.name + ".part")

    path.parent?.let(Environment.fileSystem::createDirectories)

    try {
        Environment.fileSystem.write(partPath) {
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_HTTP_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    write(packet.readBytes())
                }
            }
        }
        Environment.fileSystem.atomicMove(partPath, path)

        downloadProgress?.invoke(1.0)
    } finally {
        Environment.fileSystem.delete(partPath, mustExist = false)
    }
}
