package net.axay.pacmc.app

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import okio.Path

val ktorClient = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        })
    }
}

suspend inline fun HttpClient.downloadFile(
    url: String,
    path: Path,
    noinline downloadProgress: (suspend (Double) -> Unit)? = null,
    builder: HttpRequestBuilder.() -> Unit = {},
) = get<HttpStatement>(url) {
    builder(this)

    if (downloadProgress != null) {
        onDownload { bytesSentTotal, contentLength ->
            downloadProgress(bytesSentTotal.toDouble() / contentLength.toDouble())
        }
    }
}.execute { response ->
    val channel = response.receive<ByteReadChannel>()

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
