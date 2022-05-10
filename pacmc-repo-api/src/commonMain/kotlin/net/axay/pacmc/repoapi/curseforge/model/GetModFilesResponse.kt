package net.axay.pacmc.repoapi.curseforge.model

import kotlinx.serialization.Serializable

@Serializable
data class GetModFilesResponse(
    val data: List<File>,
)
