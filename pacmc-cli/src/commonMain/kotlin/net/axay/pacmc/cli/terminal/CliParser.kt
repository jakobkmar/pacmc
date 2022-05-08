package net.axay.pacmc.cli.terminal

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.defaultLazy
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.terminal

fun ParameterHolder.archiveIdOption(
    help: String
) = option("-a", "--archive", help = help)
    .defaultLazy { Archive.getDefault() }

fun CliktCommand.archiveIdArgument(
    help: String
) = argument("archiveIdentifier", help = help)
    .defaultLazy { Archive.getDefault() }

suspend fun Archive.Companion.terminalFromString(name: String): Archive? {
    val archive = Archive(name)
    return if (archive.exists()) archive else {
        terminal.warning("The given archive '${archive.name}' does not exist")
        if (archive.name == ".minecraft") {
            terminal.warning("Try running ${TextColors.brightWhite("pacmc init")} to automatically detect the '.minecraft' folder.")
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
