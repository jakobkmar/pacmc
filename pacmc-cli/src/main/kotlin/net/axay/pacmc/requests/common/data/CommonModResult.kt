package net.axay.pacmc.requests.common.data

import kotlinx.datetime.Instant
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.data.Repository

data class CommonModResult(
    val repository: Repository,
    val id: String,
    val slug: String,
    val name: String,
    val description: String?,
    val author: String,
    val gameVersions: List<MinecraftVersion>,
    val dateModified: Instant,
)
