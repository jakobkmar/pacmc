package net.axay.pacmc.gui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.axay.pacmc.gui.cache.producePainterCached
import net.axay.pacmc.server.model.JsonMarkup
import okio.ByteString.Companion.toByteString
import java.awt.Desktop
import java.net.URI

private class MarkupBuilder {
    private var stringBuilder: AnnotatedString.Builder? = null
    val baseFontSize = 17

    private val styles = ArrayDeque<SpanStyle>()

    inline fun withStyle(style: SpanStyle, block: () -> Unit) {
        styles.addLast(style)
        stringBuilder?.pushStyle(style)
        block()
        stringBuilder?.pop()
        styles.removeLast()
    }

    fun getOrCreateString(): AnnotatedString.Builder {
        if (stringBuilder == null) {
            stringBuilder = AnnotatedString.Builder().apply {
                pushStyle(SpanStyle(fontSize = baseFontSize.sp))
                styles.forEach { pushStyle(it) }
            }
        }
        return stringBuilder!!
    }

    @Composable
    fun endString() {
        stringBuilder?.let { builder ->
            builder.pop() // pop base font size
            repeat(styles.size) {
                builder.pop()
            }
            val annotatedString = builder.toAnnotatedString()
            if (annotatedString.isNotEmpty())
                Text(annotatedString)
        }
        stringBuilder = null
    }
}

private val LocalListLevel = compositionLocalOf { 0 }

@Composable
fun JsonMarkup(node: JsonMarkup.RootNode) = MarkupBuilder().JsonMarkup(node)

@Composable
private fun MarkupBuilder.JsonMarkup(node: JsonMarkup.Node) {
    if (node !is JsonMarkup.StyleNode && node !is JsonMarkup.TextNode) {
        endString()
    }

    when (node) {
        is JsonMarkup.RootNode -> {
            Column {
                renderNodes(node.contents)
                endString()
            }
        }
        is JsonMarkup.ImageNode -> {
            val imageUrl = node.url.let { if (it.startsWith("/content")) "https://www.minecraft.net${it}" else it }

            val painter = producePainterCached(
                imageUrl,
                "web_content",
                remember(imageUrl) { imageUrl.toByteArray().toByteString().sha256().hex() }
            )

            if (painter != null) {
                Image(painter, node.url, Modifier.fillMaxWidth())
            }
        }
        is JsonMarkup.LinkNode -> {
            Box(
                modifier = Modifier.clickable {
                    Desktop.getDesktop().browse(URI(node.url))
                }
            ) {
                renderNodes(node.contents)
                if (node.video) {
                    Box(Modifier.align(Alignment.Center).size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f))) {
                        Icon(
                            Icons.Default.PlayArrow,
                            "Play video",
                            Modifier.align(Alignment.Center).size(50.dp),
                            tint = Color.Red
                        )
                    }
                }
            }
        }
        is JsonMarkup.ListNode -> {
            val listLevel = LocalListLevel.current

            Column {
                node.elements.forEach { listPart ->
                    Row {
                        Text(
                            text = listOf("•", "◦", "▸", "▹").run { getOrNull(listLevel) ?: last() } + " ",
                            modifier = Modifier.padding(start = (3 * listLevel).dp),
                        )
                        Column {
                            CompositionLocalProvider(LocalListLevel provides listLevel + 1) {
                                renderNodes(listPart)
                                endString()
                            }
                        }
                    }
                }
            }
        }
        is JsonMarkup.ParagraphNode -> {
            Column(Modifier.padding(vertical = 10.dp)) {
                renderNodes(node.contents)
                endString()
            }
        }
        is JsonMarkup.QuoteNode -> {
            Row {
                Box(Modifier.fillMaxHeight().width(4.dp).background(Color.Black))
                Column {
                    renderNodes(node.contents)
                    endString()
                }
            }
        }
        is JsonMarkup.HeadingNode -> {
            withStyle(SpanStyle(fontSize = (baseFontSize + (node.size * 3)).sp)) {
                renderNodes(node.contents)
            }
        }
        is JsonMarkup.Preformatted -> {
            Column(
                Modifier
                    .background(Color.LightGray.copy(alpha = .5f))
                    .padding(5.dp)
            ) {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    renderNodes(node.contents)
                }
            }
        }
        is JsonMarkup.StyleNode -> {
            withStyle(node.getSpanStyle()) {
                renderNodes(node.contents)
            }
        }
        is JsonMarkup.TextNode -> {
            getOrCreateString().append(node.text)
        }
    }
}

@Composable
private fun MarkupBuilder.renderNodes(nodes: Iterable<JsonMarkup.Node>) {
    nodes.forEach {
        JsonMarkup(it)
    }
}

private fun JsonMarkup.StyleNode.getSpanStyle() = when (this) {
    is JsonMarkup.StyleNode.Bold, is JsonMarkup.StyleNode.Emphasized, is JsonMarkup.StyleNode.Important -> SpanStyle(fontWeight = FontWeight.Bold)
    is JsonMarkup.StyleNode.Code -> SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        background = Color.LightGray.copy(alpha = .5f)
    )
    is JsonMarkup.StyleNode.Strikethrough, is JsonMarkup.StyleNode.Deleted -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    is JsonMarkup.StyleNode.Underline -> SpanStyle(textDecoration = TextDecoration.Underline)
    is JsonMarkup.StyleNode.Italic, is JsonMarkup.StyleNode.Inserted -> SpanStyle(fontStyle = FontStyle.Italic)
    is JsonMarkup.StyleNode.Marked -> SpanStyle(background = Color.Yellow.copy(alpha = .5f))
    is JsonMarkup.StyleNode.Subscript -> SpanStyle(
        baselineShift = BaselineShift(-0.2f),
        // TODO this should be relative to current font size
        fontSize = 10.sp
    )
    is JsonMarkup.StyleNode.Superscript -> SpanStyle(
        baselineShift = BaselineShift.Superscript,
        fontSize = 10.sp
    )
    is JsonMarkup.StyleNode.Small -> TODO()
}
