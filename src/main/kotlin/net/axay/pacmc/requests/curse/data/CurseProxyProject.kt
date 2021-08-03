package net.axay.pacmc.requests.curse.data

import kotlinx.serialization.Serializable
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.data.ReleaseType
import net.axay.pacmc.requests.common.CommonConvertible
import net.axay.pacmc.requests.common.data.CommonModResult

@Serializable
data class CurseProxyProject(
    val id: Int,
    val name: String,
    val authors: List<Author>,
    val summary: String,
    val gameVersionLatestFiles: List<GameVersionLatestFile>,
    val gamePopularityRank: Long,
    val dateReleased: String,
) : CommonConvertible<CommonModResult> {
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

    override fun convertToCommon() = CommonModResult(
        "curseforge",
        id.toString(),
        name,
        summary,
        authors.first().name,
        gameVersionLatestFiles.mapNotNull { MinecraftVersion.fromString(it.gameVersion) }
    )
}
