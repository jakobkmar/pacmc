package net.axay.pacmc.repoapi.curseforge.model

import kotlinx.serialization.Serializable

@Serializable
data class Mod(
    val id: Int,
    val name: String,
    val slug: String,
    val summary: String,
    val authors: List<ModAuthor>,
    val logo: ModAsset,
    val latestFilesIndexes: List<FileIndex>,
)
