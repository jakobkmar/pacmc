package net.axay.pacmc.server.routes

import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.axay.pacmc.server.database.db
import net.axay.pacmc.server.model.MinecraftArticle
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.*

fun Routing.routeNews() = route("/news/minecraft") {
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
            add(Document("\$set", Document("id", Document("\$toString", "\$_id"))))
        }
        val articles = db.minecraftFeed.aggregate<MinecraftArticle.SearchResult>(pipeline)
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
