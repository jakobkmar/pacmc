package net.axay.pacmc.requests.common.data

import net.axay.pacmc.data.MinecraftVersion

data class CommonModResult(
    val repository: String,
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val gameVersions: List<MinecraftVersion>,
)
