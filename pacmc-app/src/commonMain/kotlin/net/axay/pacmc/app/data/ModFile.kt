package net.axay.pacmc.app.data

class ModFile(
    val repositoryPart: String,
    val slugPart: String?,
    val idPart: String,
) {
    val fileName get() = "${repositoryPart}_${slugPart ?: "unknown"}_${idPart}.jar"
}
