package net.axay.pacmc.server.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class JsonMarkup {
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
        @Serializable @SerialName("style.code") class Code(override val contents: List<Node>) : StyleNode()
    }

    @Serializable
    sealed class TextNode : Node() {
        abstract val text: String

        @Serializable
        @SerialName("text.raw")
        class Raw(
            override val text: String,
        ) : TextNode()

        @Serializable
        @SerialName("text.link")
        class Link(
            override val text: String,
            val url: String,
        ) : TextNode()
    }

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

    @Serializable
    class Preformatted(
        val contents: List<Node>,
    ) : Node()
}
