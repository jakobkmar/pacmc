package net.axay.pacmc.requests

import kotlinx.serialization.Serializable

object CurseProxy {
    @Serializable
    data class Project(
        val id: String,
        val name: String,
        val authors: List<Author>,
        val summary: String,
        val dateReleased: String,
    ) {
        @Serializable
        data class Author(
            val name: String,
        )
    }
}
