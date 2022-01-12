package net.axay.pacmc.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
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
