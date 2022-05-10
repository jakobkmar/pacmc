package net.axay.pacmc.common.data

enum class ModLoader(
    val displayName: String,
    val curseforgeId: Int?,
) {
    QUILT("Quilt", null),
    FABRIC("Fabric", 4),
    FORGE("Forge", 1);

    val identifier = name.lowercase()
}
