package net.axay.pacmc.app.data

class ModFile(
    val repositoryPart: String,
    val slugPart: String?,
    val idPart: String,
    val extension: String = "jar",
) {
    val fileName get() = "${slugPart?.plus("_").orEmpty()}${repositoryPart}_${idPart}.pacmc.${extension}"
}
