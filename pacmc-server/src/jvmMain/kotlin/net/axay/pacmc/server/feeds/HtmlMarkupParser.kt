package net.axay.pacmc.server.feeds

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jsoup.nodes.Element

object HtmlMarkupParser {
    fun parse(element: Element) = RootNode(1, parseNodes(element))

    private fun parseNodes(element: Element): List<Node> {
        return element.childNodes().flatMap { child ->
            if (child is Element) {
                val node = when (child.tagName()) {
                    // wrappers
                    "p" -> ParagraphNode(parseNodes(child))
                    "blockquote" -> QuoteNode(parseNodes(child))
                    // elements
                    "img" -> child.attr("src").ifBlank { null }?.let { ImageNode(it.trim()) }
                    // style
                    "b" -> StyleNode.Bold(parseNodes(child))
                    "strong" -> StyleNode.Important(parseNodes(child))
                    "i" -> StyleNode.Italic(parseNodes(child))
                    "em" -> StyleNode.Emphasized(parseNodes(child))
                    "s" -> StyleNode.Strikethrough(parseNodes(child))
                    "u" -> StyleNode.Underline(parseNodes(child))
                    "ins" -> StyleNode.Inserted(parseNodes(child))
                    "del" -> StyleNode.Deleted(parseNodes(child))
                    "mark" -> StyleNode.Marked(parseNodes(child))
                    "code" -> StyleNode.Code(parseNodes(child))
                    "pre" -> StyleNode.Preformatted(parseNodes(child))
                    "small" -> StyleNode.Small(parseNodes(child))
                    "sub" -> StyleNode.Subscript(parseNodes(child))
                    "sup" -> StyleNode.Superscript(parseNodes(child))
                    "span" -> when {
                        child.hasClass("strikethrough") -> StyleNode.Strikethrough(parseNodes(child))
                        child.hasClass("bedrock-server") -> StyleNode.Code(parseNodes(child))
                        else -> null
                    }
                    // headings
                    "h1", "h2", "h3", "h4", "h5", "h6" -> HeadingNode(parseNodes(child), child.tagName().last().digitToInt())
                    // lists
                    "ul", "ol" -> ListNode(child.select("> li").map { parseNodes(it) }, child.tagName() == "ol")
                    // link
                    "a" -> {
                        val videoElement = child.selectFirst("div[data-video-url]")
                        child.attr("href").ifBlank { null }?.let {
                            if (videoElement == null) {
                                LinkNode(parseNodes(child), it, false)
                            } else {
                                LinkNode(parseNodes(child), videoElement.attr("data-video-url"), true)
                            }
                        }
                    }
                    // ignore svg for now
                    "svg" -> return@flatMap emptyList()
                    else -> null
                }
                if (node != null) listOf(node) else parseNodes(child)
            } else if (child is org.jsoup.nodes.TextNode) {
                val text = child.text()
                if (text.isNotBlank()) {
                    listOf(TextNode(child.text()))
                } else emptyList()
            } else {
                emptyList()
            }
        }
    }

    @Serializable
    sealed class Node

    @Serializable
    @SerialName("root")
    class RootNode(
        val schemaVersion: Int,
        val contents: List<Node>,
    ) : Node()

    @Serializable
    @SerialName("paragraph")
    class ParagraphNode(
        val contents: List<Node>,
    ) : Node()

    @Serializable
    @SerialName("quote")
    class QuoteNode(
        val contents: List<Node>,
    ) : Node()

    @Serializable
    sealed class StyleNode : Node() {
        abstract val contents: List<Node>

        @Serializable @SerialName("style.bold") class Bold(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.important") class Important(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.italic") class Italic(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.strikethrough") class Strikethrough(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.underline") class Underline(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.emphasized") class Emphasized(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.marked") class Marked(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.small") class Small(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.deleted") class Deleted(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.inserted") class Inserted(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.subscript") class Subscript(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.superscript") class Superscript(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.preformatted") class Preformatted(override val contents: List<Node>) : StyleNode()
        @Serializable @SerialName("style.code") class Code(override val contents: List<Node>) : StyleNode()
    }

    @Serializable
    @SerialName("text")
    class TextNode(
        val text: String,
    ) : Node()

    @Serializable
    @SerialName("image")
    class ImageNode(
        val url: String,
    ) : Node()

    @Serializable
    @SerialName("heading")
    class HeadingNode(
        val contents: List<Node>,
        val size: Int,
    ) : Node()

    @Serializable
    @SerialName("list")
    class ListNode(
        val elements: List<List<Node>>,
        val ordered: Boolean,
    ) : Node()

    @Serializable
    @SerialName("link")
    class LinkNode(
        val contents: List<Node>,
        val url: String,
        val video: Boolean = false,
    ) : Node()
}
