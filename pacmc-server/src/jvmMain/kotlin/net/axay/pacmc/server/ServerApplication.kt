package net.axay.pacmc.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.server.feeds.MinecraftFeedHandler
import net.axay.pacmc.server.routes.routeIndex


fun main() {
    runBlocking(Dispatchers.Default) {
        MinecraftFeedHandler().monitor()

        embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json()
            }

            routing {
                routeIndex()
            }
        }.start(wait = true)
    }
}
