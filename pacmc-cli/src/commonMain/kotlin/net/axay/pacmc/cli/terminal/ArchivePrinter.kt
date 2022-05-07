package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.features.Archive

suspend fun Archive.terminalString(): String {
    return "${TextColors.brightWhite(name)} at ${TextColors.gray(getPath().toString())}"
}
