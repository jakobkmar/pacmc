package net.axay.pacmc.requests.data

import kotlinx.serialization.Serializable
import net.axay.pacmc.data.MinecraftVersion
import java.time.Instant

@Serializable
data class CurseProxyFile(
    val id: Int,
    val displayName: String,
    val fileName: String,
    val fileDate: String,
    val fileStatus: Int,
    val downloadUrl: String,
    val dependencies: List<Dependency>,
    val gameVersion: List<String>,
) {
    @Serializable
    data class Dependency(
        val addonId: Int,
        val type: Int,
    )

    val releaseDate: Instant by lazy { Instant.parse(fileDate) }

    val minecraftVersions by lazy { gameVersion.mapNotNull { MinecraftVersion.fromString(it) } }
}
