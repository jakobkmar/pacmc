package net.axay.pacmc.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.axay.pacmc.server.feeds.MinecraftFeedHandler
import net.axay.pacmc.server.routes.routeIndex
import net.axay.pacmc.server.routes.routeNews

fun main() {
    MinecraftFeedHandler().monitor()

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            routeIndex()
            routeNews()
        }
    }.start(wait = true)
}
