package net.axay.pacmc.server.feeds

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
)
