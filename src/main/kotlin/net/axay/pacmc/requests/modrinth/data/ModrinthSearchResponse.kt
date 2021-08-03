package net.axay.pacmc.requests.modrinth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthSearchResponse(
    val hits: List<ModrinthModResult>,
    val offset: Int,
    val limit: Int,
    @SerialName("total_hits") val totalHits: Int,
)
