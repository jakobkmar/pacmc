package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.archiveIdOption
import net.axay.pacmc.cli.terminal.terminalFromString
import net.axay.pacmc.cli.terminal.optimalTerminalString

class ListCommand : CliktCommand(
    name = "list",
    help = "List content installed to an archive",
) {
    private val archiveName by archiveIdOption("The archive which contains the content that should be listed")

    override fun run() = launchJob {
        terminal.println("The archive '$archiveName' contains the following content:")
        val archive = Archive.terminalFromString(archiveName) ?: return@launchJob

        terminal.println()
        archive.getInstalled().sortedBy { it.dependency }.forEach {
            terminal.print("  " + it.optimalTerminalString())
            if (it.dependency) {
                terminal.print(" ${TextColors.brightCyan("(dependency)")}")
            }
            terminal.println()
        }
    }
}
