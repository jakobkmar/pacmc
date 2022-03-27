package net.axay.pacmc.requests.modrinth.data

import kotlinx.serialization.Serializable

@Serializable
data class ModrinthUser(
    val id: String,
    val username: String,
)
