package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.optimalTerminalString

class ListCommand : CliktCommand(
    name = "list",
    help = "List content installed to an archive",
) {
    private val archiveName by option(
        "-a", "--archive",
        help = "The archive which contains the content that should be listed"
    )

    override fun run() = launchJob {
        val archive = Archive(archiveName!!)
        terminal.println("The archive '$archiveName' contains the following content:")
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
