package net.axay.pacmc.gui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import net.axay.pacmc.gui.cache.producePainterCached
import net.axay.pacmc.server.model.JsonMarkup
import okio.ByteString.Companion.toByteString
import java.awt.Desktop
import java.net.URI

@Composable
fun JsonMarkup(node: JsonMarkup.RootNode) = JsonMarkup(node, MarkupStyle(20f))

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun JsonMarkup(node: JsonMarkup.Node, style: MarkupStyle) {
    when (node) {
        is JsonMarkup.RootNode -> {
            Column {
                renderNodes(node.contents, style)
            }
        }
        is JsonMarkup.HeadingNode -> {
            val newStyle = style.copy(heading = node.size)
            renderNodes(node.contents, newStyle)
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
                renderNodes(node.contents, style)
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
            Column {
                node.elements.forEach { element ->
                    Row {
                        Text("●")
                        renderNodes(element, style)
                    }
                }
            }
        }
        is JsonMarkup.ParagraphNode -> {
            Box(Modifier.padding(vertical = 10.dp)) {
                renderNodes(node.contents, style)
            }
        }
        is JsonMarkup.QuoteNode -> Row {
            Box(Modifier.fillMaxHeight().width(4.dp).background(Color.Black))
            renderNodes(node.contents, style)
        }
        is JsonMarkup.StyleNode -> error("Unhandled style node")
        is JsonMarkup.TextNode -> error("Unhandled text node")
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
private fun JsonMarkupText(
    node: JsonMarkup.TextNode,
    style: MarkupStyle,
    builder: AnnotatedString.Builder,
) {
    val fontSize = if (style.heading != null && style.heading in 1..6) {
        TextUnit(style.fontSize + ((6f * 2 + 2) - style.heading * 2), TextUnitType.Sp)
    } else if (style.small) {
        TextUnit(style.fontSize - 4, TextUnitType.Sp)
    } else TextUnit(style.fontSize, TextUnitType.Sp)

    val decoration = if (style.strikethrough || style.deleted)
        TextDecoration.LineThrough
    else if (style.underline || style.inserted || node is JsonMarkup.TextNode.Link)
        TextDecoration.Underline
    else null

    builder.withStyle(SpanStyle(
        fontWeight = if (style.bold || style.emphasized || style.important) FontWeight.Bold else null,
        textDecoration = decoration,
        fontStyle = if (style.italic) FontStyle.Italic else null,
        fontSize = fontSize,
        background = if (style.marked) Color.Yellow else Color.Unspecified,
        color = if (node is JsonMarkup.TextNode.Link) MaterialTheme.colors.secondary else Color.Unspecified,
    )) {
        if (builder.length == 0)
            append(node.text.trimStart())
        else
            append(node.text)
    }
}

@Composable
private fun renderNodes(
    nodes: List<JsonMarkup.Node>,
    style: MarkupStyle,
    stringBuilder: AnnotatedString.Builder? = null,
): Boolean {
    var currentStringBuilder: AnnotatedString.Builder = stringBuilder ?: AnnotatedString.Builder()

    @Composable
    fun endString() {
        currentStringBuilder.toAnnotatedString().let { if (it.isNotEmpty()) Text(it) }
        currentStringBuilder = AnnotatedString.Builder()
    }

    nodes.forEach { node ->
        when (node) {
            is JsonMarkup.TextNode -> {
                JsonMarkupText(node, style, currentStringBuilder)
            }
            is JsonMarkup.StyleNode -> {
                renderNodes(node.contents, style.copyWith(node), currentStringBuilder)
            }
            else -> {
                endString()
                JsonMarkup(node, style)
            }
        }
    }

    return if (currentStringBuilder != stringBuilder) {
        endString()
        false // the string builder has changed
    } else {
        true // the string builder is still the same
    }
}

private data class MarkupStyle(
    val fontSize: Float,
    val heading: Int? = null,
    val bold: Boolean = false,
    val code: Boolean = false,
    val deleted: Boolean = false,
    val emphasized: Boolean = false,
    val important: Boolean = false,
    val inserted: Boolean = false,
    val italic: Boolean = false,
    val marked: Boolean = false,
    val preformatted: Boolean = false,
    val small: Boolean = false,
    val strikethrough: Boolean = false,
    val subscript: Boolean = false,
    val superscript: Boolean = false,
    val underline: Boolean = false,
) {
    fun copyWith(node: JsonMarkup.StyleNode) = when (node) {
        is JsonMarkup.StyleNode.Bold -> copy(bold = true)
        is JsonMarkup.StyleNode.Code -> copy(code = true)
        is JsonMarkup.StyleNode.Deleted -> copy(deleted = true)
        is JsonMarkup.StyleNode.Emphasized -> copy(emphasized = true)
        is JsonMarkup.StyleNode.Important -> copy(important = true)
        is JsonMarkup.StyleNode.Inserted -> copy(inserted = true)
        is JsonMarkup.StyleNode.Italic -> copy(italic = true)
        is JsonMarkup.StyleNode.Marked -> copy(marked = true)
        is JsonMarkup.StyleNode.Preformatted -> copy(preformatted = true)
        is JsonMarkup.StyleNode.Small -> copy(small = true)
        is JsonMarkup.StyleNode.Strikethrough -> copy(strikethrough = true)
        is JsonMarkup.StyleNode.Subscript -> copy(subscript = true)
        is JsonMarkup.StyleNode.Superscript -> copy(superscript = true)
        is JsonMarkup.StyleNode.Underline -> copy(underline = true)
    }
}
