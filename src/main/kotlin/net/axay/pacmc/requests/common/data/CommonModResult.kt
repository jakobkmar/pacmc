package net.axay.pacmc.requests.common.data

import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.data.Repository

data class CommonModResult(
    val repository: Repository,
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val gameVersions: List<MinecraftVersion>,
)
