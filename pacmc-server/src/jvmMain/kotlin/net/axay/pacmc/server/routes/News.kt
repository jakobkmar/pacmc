package net.axay.pacmc.server.routes

import com.mongodb.client.model.Filters
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.axay.pacmc.server.database.db
import net.axay.pacmc.server.feeds.MinecraftArticle
import org.bson.types.ObjectId
import org.litote.kmongo.*

fun Routing.routeNews() {
    route("/news/minecraft") {
        get {
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

        get("/{id}") {
            val params = call.parameters
            val id = params["id"]

            if (id != null) {
                val feed = db.minecraftFeed.findOne(Filters.eq(ObjectId(id)))
                if (feed != null) {
                    call.respond(feed)
                    return@get
                }
            }

            call.response.status(HttpStatusCode.NotFound)
        }
    }
}

@Serializable
private data class SearchResult(
    @Contextual @SerialName("_id") val id: Id<SearchResult>,
    val url: String,
    val datePublished: Instant,
    val category: String?,
    val previewImage: String?,
    val headerImage: String?,
    val title: String,
    val description: String?,
    val author: String,
)
