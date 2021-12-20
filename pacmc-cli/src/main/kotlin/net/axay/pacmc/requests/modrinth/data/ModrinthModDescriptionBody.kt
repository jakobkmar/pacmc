package net.axay.pacmc.requests.modrinth.data

import kotlinx.serialization.Serializable

@Serializable
data class ModrinthModDescriptionBody(
    val title: String,
    val description: String,
    val body: String,
)
