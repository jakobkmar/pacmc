package net.axay.pacmc.common.data

enum class ModLoader(
    val displayName: String,
    val curseforgeId: Int?,
    val compatibleLoaders: List<ModLoader> = emptyList(),
) {
    QUILT("Quilt", null),
    FABRIC("Fabric", 4),
    FORGE("Forge", 1);
    FORGE("Forge", 1),
    LITELOADER("LiteLoader", 3),
    RIFT("Rift", null),
    MODLOADER("Risugami's ModLoader", null),
    BUKKIT("Bukkit", null),
    SPIGOT("Spigot", null, listOf(BUKKIT)),
    SPONGE("Sponge", null),
    PAPER("Paper", null, listOf(BUKKIT, SPIGOT)),
    PURPUR("Purpur", null, listOf(BUKKIT, SPIGOT, PAPER)),
    BUNGEECORD("BungeeCord", null),
    VELOCITY("Velocity", null),
    WATERFALL("Waterfall", null, listOf(BUNGEECORD));

    val identifier = name.lowercase()
}
