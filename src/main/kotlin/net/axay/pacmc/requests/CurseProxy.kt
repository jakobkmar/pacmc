package net.axay.pacmc.requests

import kotlinx.serialization.Serializable

object CurseProxy {
    @Serializable
    data class Project(
        val id: String,
        val name: String,
        val authors: List<Author>,
        val summary: String,
        val gameVersionLatestFiles: List<GameVersionLatestFile>,
        val dateReleased: String,
    ) {
        @Serializable
        data class Author(
            val name: String,
        )

        @Serializable
        data class GameVersionLatestFile(
            val gameVersion: String,
            val projectFileName: String,
            val fileType: Int,
        )

        val latestVersion: String
            get() {
                val versionString = kotlin.run {
                    gameVersionLatestFiles.firstOrNull { it.fileType == 1 }
                        ?: gameVersionLatestFiles.firstOrNull { it.fileType == 2 }
                        ?: gameVersionLatestFiles.first()
                }.projectFileName
                return versionString.removeSuffix(".jar")
            }
    }
}
