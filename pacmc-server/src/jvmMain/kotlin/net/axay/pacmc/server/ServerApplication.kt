package net.axay.pacmc.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import net.axay.pacmc.server.feeds.MinecraftFeedHandler
import net.axay.pacmc.server.routes.routeIndex
import net.axay.pacmc.server.routes.routeNews
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule

fun main() {
    MinecraftFeedHandler().monitor()

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json {
                serializersModule = IdKotlinXSerializationModule
            })
        }

        routing {
            routeIndex()
            routeNews()
        }
    }.start(wait = true)
}
