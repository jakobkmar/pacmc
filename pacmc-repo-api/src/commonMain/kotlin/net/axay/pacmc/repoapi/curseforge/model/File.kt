package net.axay.pacmc.repoapi.curseforge.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class File(
    val id: Int,
    val modId: Int,
    val fileDate: Instant,
    val fileName: String,
    val displayName: String,
    val sortableGameVersions: List<SortableGameVersion>,
    val dependencies: List<FileDependency>,
    val releaseType: Int,
    val downloadUrl: String,
)
