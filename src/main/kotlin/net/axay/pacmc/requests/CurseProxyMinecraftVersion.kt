package net.axay.pacmc.requests

import kotlinx.serialization.Serializable

@Serializable
data class CurseProxyMinecraftVersion(
    val versionString: String,
)
