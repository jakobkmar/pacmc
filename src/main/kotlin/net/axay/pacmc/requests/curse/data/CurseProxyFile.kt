package net.axay.pacmc.requests.curse.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.data.ReleaseType
import net.axay.pacmc.data.Repository
import net.axay.pacmc.requests.common.CommonConvertible
import net.axay.pacmc.requests.common.data.CommonModVersion

@Serializable
data class CurseProxyFile(
    val id: Int,
    val displayName: String,
    val fileName: String,
    val fileDate: String,
    val releaseType: Int,
    val fileStatus: Int,
    val downloadUrl: String,
    val dependencies: List<Dependency>,
    val gameVersion: List<String>,
) : CommonConvertible<CommonModVersion> {
    @Serializable
    data class Dependency(
        val addonId: Int,
        val type: Int,
    )

    companion object {
        private val versionChars = arrayOf('.', '-', '+', '_', ' ')
        private val possibleLoaders = arrayOf("Fabric", "Forge", "Rift")
    }

    override fun convertToCommon() = CommonModVersion(
        Repository.CURSEFORGE,
        id.toString(),
        displayName,
        fileName.removeSuffix(".jar").lowercase().trim { it in versionChars || it.isLetter() },
        null,
        Instant.parse(fileDate),
        when (releaseType) {
            1 -> ReleaseType.RELEASE
            2 -> ReleaseType.BETA
            3 -> ReleaseType.ALPHA
            else -> error("Received an invalid or unknown release type (number $releaseType) from Curseforge")
        },
        gameVersion.mapNotNull { MinecraftVersion.fromString(it) },
        gameVersion.filter { it in possibleLoaders }.map { it.lowercase() },
        listOf(CommonModVersion.File(downloadUrl, fileName)),
        dependencies.filter { it.type == 3 }.map { CommonModVersion.Dependency(it.addonId.toString(), null) }
    )
}
