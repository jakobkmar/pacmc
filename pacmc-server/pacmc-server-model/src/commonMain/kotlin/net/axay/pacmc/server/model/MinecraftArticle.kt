package net.axay.pacmc.server.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MinecraftArticle(
    val url: String,
    val datePublished: Instant,
    val category: String?,
    val previewImage: String?,
    val headerImage: String?,
    val title: String,
    val description: String?,
    val author: String,
    val contentHtml: String,
    val contentJson: String,
) {
    @Serializable
    data class SearchResult(
        val id: String,
        val url: String,
        val datePublished: Instant,
        val category: String?,
        val previewImage: String?,
        val headerImage: String?,
        val title: String,
        val description: String?,
        val author: String,
    )
}
