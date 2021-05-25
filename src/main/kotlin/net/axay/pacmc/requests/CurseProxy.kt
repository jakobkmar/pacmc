package net.axay.pacmc.requests

import kotlinx.serialization.Serializable
import net.axay.pacmc.data.ReleaseType

object CurseProxy {
    @Serializable
    data class Project(
        val id: Int,
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

        fun getLatestVersion(gameVersion: String?): Pair<String, ReleaseType?>? {
            val files = gameVersionLatestFiles.let { files ->
                if (gameVersion != null) {
                    files.filter { it.gameVersion == gameVersion }.ifEmpty {
                        val majorVersion = gameVersion.split('.').take(2).joinToString(".")
                        files.filter { it.gameVersion.startsWith(majorVersion) }
                    }
                } else files
            }
            return kotlin.run {
                files.firstOrNull { it.fileType == 1 }
                    ?: files.firstOrNull { it.fileType == 2 }
                    ?: files.firstOrNull()
            }?.let { it.projectFileName.removeSuffix(".jar") to ReleaseType.fromInt(it.fileType) }
        }
    }

    @Serializable
    data class MinecraftVersion(
        val versionString: String,
    )
}
