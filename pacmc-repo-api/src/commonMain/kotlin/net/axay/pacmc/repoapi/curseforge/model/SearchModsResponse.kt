package net.axay.pacmc.repoapi.curseforge.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchModsResponse(
    val data: List<Mod>,
)
