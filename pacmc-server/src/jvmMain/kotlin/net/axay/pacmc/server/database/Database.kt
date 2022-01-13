package net.axay.pacmc.server.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Indexes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.axay.pacmc.server.model.MinecraftArticle
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.textIndex

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
            minecraftFeed.ensureIndex(Indexes.compoundIndex(
                MinecraftArticle::title.textIndex(),
                MinecraftArticle::description.textIndex(),
            ))
            minecraftFeed.ensureIndex(MinecraftArticle::datePublished)
        }
    }
}
