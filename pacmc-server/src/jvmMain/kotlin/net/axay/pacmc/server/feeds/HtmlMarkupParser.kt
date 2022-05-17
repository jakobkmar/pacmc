package net.axay.pacmc.server.feeds

import net.axay.pacmc.server.model.JsonMarkup.*
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
                    "pre" -> Preformatted(parseNodes(child))
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
                        child.attr("href").ifBlank { null }?.let { link ->
                            if (child.childNodes().singleOrNull() is org.jsoup.nodes.TextNode) {
                                TextNode.Link(child.wholeText(), link)
                            } else {
                                val videoElement = child.selectFirst("div[data-video-url]")
                                if (videoElement == null) {
                                    LinkNode(parseNodes(child), link, false)
                                } else {
                                    LinkNode(parseNodes(child), videoElement.attr("data-video-url"), true)
                                }
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
                    listOf(TextNode.Raw(child.text()))
                } else emptyList()
            } else {
                emptyList()
            }
        }
    }
}
