package net.axay.pacmc.requests.data

import kotlinx.serialization.Serializable

@Serializable
data class CurseProxyMinecraftVersion(
    val versionString: String,
)
