package net.axay.pacmc.requests.data

import kotlinx.serialization.Serializable

@Serializable
data class CurseProxyFile(
    val id: Int,
    val displayName: String,
    val fileName: String,
    val fileStatus: Int,
    val downloadUrl: String,
    val dependencies: List<Dependency>
) {
    @Serializable
    data class Dependency(
        val addonId: Int,
        val type: Int,
    )
}
