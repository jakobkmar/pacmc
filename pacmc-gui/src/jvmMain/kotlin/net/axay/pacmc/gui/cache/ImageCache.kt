package net.axay.pacmc.gui.cache

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import co.touchlab.kermit.Logger
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.downloadFile
import net.axay.pacmc.app.ktorClient
import org.jetbrains.skia.AnimationFrameInfo
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data

private fun cachePath(group: String, name: String) =
    Environment.cacheDir.resolve("gui/images/${group}/${name}")

private sealed class PainterHolder

private class SinglePainterHolder(val painter: Painter) : PainterHolder()

private class AnimationPainterHolder(
    val painters: List<Painter>,
    val framesInfo: Array<AnimationFrameInfo>,
) : PainterHolder()

private object InvalidPainterHolder : PainterHolder()

@Composable
fun producePainterCached(
    url: String,
    cacheGroup: String,
    cacheName: String,
    maxAgeHours: Int = 24,
): Painter? {
    val density = LocalDensity.current

    val painterHolder by produceState<PainterHolder?>(null, key1 = url) {
        value = withContext(Dispatchers.IO) {
            val extension = url.substringAfterLast('.')
            val filePath = cachePath(cacheGroup, "${cacheName}.${extension}")

            val download = if (!Environment.fileSystem.exists(filePath)) {
                true
            } else {
                val cacheAgeHours = Environment.fileSystem.metadata(filePath).lastModifiedAtMillis?.let {
                    Clock.System.now().minus(Instant.fromEpochMilliseconds(it))
                }?.inWholeHours
                if (cacheAgeHours == null) true else cacheAgeHours >= maxAgeHours
            }

            if (download) {
                try {
                    ktorClient.downloadFile(url, filePath) {
                        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:96.0) Gecko/20100101 Firefox/96.0")
                    }
                } catch (exc: Exception) {
                    Logger.w("Failed to download image $url (${exc.message})")
                }
            }

            if (!Environment.fileSystem.exists(filePath))
                return@withContext null

            Environment.fileSystem.read(filePath) {
                val stream = inputStream().buffered()
                try {
                    when (extension) {
                        "svg" -> SinglePainterHolder(loadSvgPainter(stream, density))
                        "gif" -> {
                            val bytes = stream.readAllBytes()
                            val codec = Codec.makeFromData(Data.makeFromBytes(bytes))

                            if (codec.frameCount > 1) {
                                AnimationPainterHolder(
                                    (0 until codec.frameCount).map { frameIndex ->
                                        val bitmap = Bitmap()
                                        bitmap.allocPixels(codec.imageInfo)
                                        codec.readPixels(bitmap, frameIndex)
                                        BitmapPainter(bitmap.asComposeImageBitmap())
                                    },
                                    codec.framesInfo
                                )
                            } else {
                                SinglePainterHolder(BitmapPainter(loadImageBitmap(bytes.inputStream())))
                            }
                        }
                        else -> SinglePainterHolder(BitmapPainter(loadImageBitmap(stream)))
                    }
                } catch (exc: Exception) {
                    Logger.e("Failed to load image $url (${exc::class.simpleName}) (${exc.message})")
                    InvalidPainterHolder
                }
            }
        }
    }

    return when (val currentPainterHolder = painterHolder) {
        is SinglePainterHolder -> currentPainterHolder.painter
        is AnimationPainterHolder -> {
            val transition = rememberInfiniteTransition()
            val frameIndex by transition.animateValue(
                initialValue = 0,
                targetValue = currentPainterHolder.painters.lastIndex,
                Int.VectorConverter,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 0
                        for ((index, frame) in currentPainterHolder.framesInfo.withIndex()) {
                            index at durationMillis
                            durationMillis += frame.duration
                        }
                    }
                )
            )

            currentPainterHolder.painters[frameIndex]
        }
        is InvalidPainterHolder, null -> null
    }
}
