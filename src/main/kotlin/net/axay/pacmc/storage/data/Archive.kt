package net.axay.pacmc.storage.data

import net.axay.pacmc.data.MinecraftVersion

data class Archive(
    val name: String,
    val path: String,
    val gameVersion: String,
) {
    val minecraftVersion by lazy { MinecraftVersion.fromString(gameVersion)!! }
}
