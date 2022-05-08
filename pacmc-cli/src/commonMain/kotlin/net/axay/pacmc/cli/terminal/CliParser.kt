package net.axay.pacmc.cli.terminal

import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.terminal

suspend fun Archive.Companion.fromString(name: String?): Archive? {
    val archive = Archive(name ?: getDefault())
    return if (archive.exists()) archive else {
        terminal.warning("The given archive '$name' does not exist")
        null
    }
}
