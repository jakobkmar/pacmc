package net.axay.pacmc.requests.curse.data

import kotlinx.serialization.Serializable

@Serializable
data class CurseProxyMinecraftVersion(
    val versionString: String,
)
