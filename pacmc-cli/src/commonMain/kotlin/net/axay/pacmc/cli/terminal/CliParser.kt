package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.common.data.MinecraftVersion
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.terminal

suspend fun Archive.Companion.terminalFromString(name: String): Archive? {
    val archive = Archive(name)
    return if (archive.exists()) archive else {
        terminal.warning("The given archive '${archive.name}' does not exist")
        if (archive.name == ".minecraft") {
            terminal.warning("Try running ${TextColors.brightWhite("pacmc archive init")} to automatically detect the '.minecraft' folder.")
        }
        null
    }
}

fun MinecraftVersion.Companion.terminalFromString(version: String): MinecraftVersion? {
    val gameVersion = fromString(version)
    if (gameVersion == null) {
        terminal.warning("The given game version '$version' is invalid")
        return null
    }
    return gameVersion
}
