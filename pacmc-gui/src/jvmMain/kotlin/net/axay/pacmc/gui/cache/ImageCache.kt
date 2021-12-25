package net.axay.pacmc.gui.cache

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.ktorClient

object ImageCache {
    private fun cachePath(group: String, name: String) =
        Environment.cacheDir.resolve("gui/images/${group}/${name}")

    suspend fun loadProjectIcon(
        url: String,
        modId: ModId,
        density: Density,
    ): Painter? {
        return try {
            val extension = url.substringAfterLast('.')
            val filePath = cachePath(
                "icons",
                "${modId.repository.shortForm}_${modId.id}." + extension
            )

            val download = if (!Environment.fileSystem.exists(filePath)) {
                true
            } else {
                val cacheAgeHours = Environment.fileSystem.metadata(filePath).lastModifiedAtMillis?.let {
                    Clock.System.now().minus(Instant.fromEpochMilliseconds(it))
                }?.inWholeHours
                if (cacheAgeHours == null) true else cacheAgeHours >= 24
            }

            if (download) {
                val bytes = ktorClient.get<HttpResponse>(url).content.toByteArray()
                Environment.fileSystem.createDirectories(filePath.parent!!)
                Environment.fileSystem.write(filePath) { write(bytes) }
            }

            Environment.fileSystem.read(filePath) {
                val stream = inputStream().buffered()
                when (extension) {
                    "svg" -> loadSvgPainter(stream, density)
                    else -> BitmapPainter(loadImageBitmap(stream))
                }
            }
        } catch (exc: Exception) {
            exc.printStackTrace()
            null
        }
    }
}
