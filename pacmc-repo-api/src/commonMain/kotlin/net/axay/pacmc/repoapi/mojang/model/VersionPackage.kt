package net.axay.pacmc.repoapi.mojang.model

import kotlinx.serialization.Serializable

@Serializable
data class VersionPackage(
    val arguments: Arguments,
    val assetIndex: AssetIndex,
    val downloads: Downloads,
    val libraries: List<Library>,
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

    @Serializable
    data class Library(
        val downloads: Downloads,
        val natives: Map<String, String>? = null,
        val rules: List<Rule>? = null,
    ) {
        @Serializable
        data class Downloads(
            val artifact: Artifact,
            val classifiers: Map<String, Artifact>,
        ) {
            @Serializable
            data class Artifact(
                val path: String,
                val url: String,
            )
        }
    }

    @Serializable
    data class Rule(
        val action: String,
        val os: Os? = null,
        val features: Map<String, Boolean>? = null,
    ) {
        @Serializable
        data class Os(val name: String)
    }
}
