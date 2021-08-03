package net.axay.pacmc.requests.common.data

import kotlinx.datetime.Instant
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.data.ReleaseType
import net.axay.pacmc.data.Repository

data class CommonModVersion(
    val repository: Repository,
    val id: String,
    val name: String,
    val number: String,
    val changelog: String?,
    val datePublished: Instant,
    val releaseType: ReleaseType,
    val gameVersions: List<MinecraftVersion>,
    val loaders: List<String>,
    val files: List<File>,
    val dependencies: List<Dependency>
) {
    data class File(val url: String, val filename: String)

    data class Dependency(val modId: String, val versionId: String?)
}
