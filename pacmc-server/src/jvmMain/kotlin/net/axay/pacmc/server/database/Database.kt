package net.axay.pacmc.server.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.axay.pacmc.server.feeds.MinecraftArticle
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val db = Database

object Database {
    private val database = KMongo.createClient(
        MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(
                System.getenv("MONGO_CONNECTION_STRING")
                    ?: "mongodb://localhost:27017"
            ))
            .build()
    ).coroutine.getDatabase(System.getenv("MONGO_DATABASE") ?: "pacmc")

    val minecraftFeed = database.getCollection<MinecraftArticle>("minecraftFeed")

    init {
        CoroutineScope(Dispatchers.Default).launch {
            minecraftFeed.ensureUniqueIndex(MinecraftArticle::url)
        }
    }
}
