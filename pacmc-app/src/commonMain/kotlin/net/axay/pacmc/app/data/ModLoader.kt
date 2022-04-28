package net.axay.pacmc.app.data

enum class ModLoader(
    val displayName: String,
) {
    QUILT("Quilt"),
    FABRIC("Fabric"),
    FORGE("Forge");

    val identifier = name.lowercase()
}
