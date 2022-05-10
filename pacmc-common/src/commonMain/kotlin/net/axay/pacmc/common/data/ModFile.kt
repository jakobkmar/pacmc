package net.axay.pacmc.common.data

import okio.Path

class ModFile(
    val repositoryPart: String,
    val slugPart: String?,
    val idPart: String,
    val extension: String = "jar",
) {
    val fileName get() = "${slugPart?.plus("_").orEmpty()}${repositoryPart}_${idPart}.pacmc.${extension}"

    companion object {
        fun modIdFromPath(path: Path): ModId? {
            val extension = path.name.takeLastWhile { it != '.' }
            val fileName = path.name.removeSuffix(".pacmc.$extension")

            val idPart = fileName.takeLastWhile { it != '_' }
            val repositoryPart = fileName.removeSuffix("_${idPart}")

            return ModId(
                Repository.values().find { it.shortForm == repositoryPart } ?: return null,
                idPart
            )
        }
    }
}
