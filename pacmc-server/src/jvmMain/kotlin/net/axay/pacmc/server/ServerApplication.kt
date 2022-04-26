package net.axay.pacmc.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
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
