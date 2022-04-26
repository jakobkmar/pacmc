package net.axay.pacmc.server.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Routing.routeIndex() {
    get("/") {
        call.respond(IndexInfo(
            "pacmc API",
            "https://github.com/jakobkmar/pacmc"
        ))
    }
}

@Serializable
private class IndexInfo(
    val name: String,
    val infoUrl: String,
)
