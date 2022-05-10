package net.axay.pacmc.repoapi.curseforge.model

import kotlinx.serialization.Serializable

@Serializable
data class FileDependency(
    val modId: Int,
    val relationType: Int,
)
