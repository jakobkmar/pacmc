package net.axay.pacmc.repoapi.curseforge.model

import kotlinx.serialization.Serializable

@Serializable
data class GetModFileResponse(
    val data: File,
)
