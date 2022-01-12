package net.axay.pacmc.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.axay.pacmc.server.database.db
import net.axay.pacmc.server.feeds.MinecraftArticle
import org.litote.kmongo.*

fun Routing.routeNews() {
    get("/news/minecraft") {
        val params = call.request.queryParameters
        val query = params["query"]

        val pipeline = buildList {
            if (query != null) {
                add(match(text(query)))
                add(sort(MinecraftArticle::title.sortByMetaTextScore()))
            } else {
                add(sort(descending(MinecraftArticle::datePublished)))
            }
            add(project(exclude(MinecraftArticle::contentHtml, MinecraftArticle::contentJson)))
        }

        val articles = db.minecraftFeed.aggregate<SearchResult>(pipeline)
        call.respond(articles.toList())
    }
}

@Serializable
private data class SearchResult(
    val url: String,
    val datePublished: Instant,
    val category: String?,
    val previewImage: String?,
    val headerImage: String?,
    val title: String,
    val description: String?,
    val author: String,
)
