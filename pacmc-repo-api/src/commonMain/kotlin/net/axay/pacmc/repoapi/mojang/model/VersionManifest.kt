package net.axay.pacmc.repoapi.mojang.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersionManifest(
    val latest: Latest,
    val versions: List<Version>,
) {
    @Serializable
    data class Latest(
        val release: String,
        val snapshot: String,
    )

    @Serializable
    data class Version(
        val id: String,
        val type: Type,
        val url: String,
        val time: Instant,
        val releaseTime: Instant,
    ) {
        @Serializable
        enum class Type {
            @SerialName("release") RELEASE,
            @SerialName("snapshot") SNAPSHOT;
        }
    }
}
