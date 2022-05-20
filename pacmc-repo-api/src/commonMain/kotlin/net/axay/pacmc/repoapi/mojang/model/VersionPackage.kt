package net.axay.pacmc.repoapi.mojang.model

import kotlinx.serialization.Serializable

@Serializable
data class VersionPackage(
    val arguments: Arguments,
    val assetIndex: AssetIndex,
    val downloads: Downloads,
) {
    @Serializable
    data class Arguments(
        val game: List<Argument>,
        val jvm: List<Argument>,
    ) {
        @Serializable
        class Argument()
    }

    @Serializable
    data class AssetIndex(
        val url: String,
    )

    @Serializable
    data class Downloads(
        val client: Download,
        val server: Download,
    ) {
        @Serializable
        data class Download(
            val url: String,
        )
    }
}
