package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.*

class RemoveCommand : CliktCommand(
    name = "remove",
    help = "Remove content from an archive",
) {
    private val modSlugNames by argument(
        "mods",
        help = "The slugs of mods which should be installed, optionally prefixed with the repository"
    ).multiple()

    private val archiveName by archiveIdOption("The archive where the content should be removed")

    private val yesFlag by yesFlag()

    override fun run() = launchJob {
        terminal.println("Resolving effects of removal...")
        val archive = Archive.terminalFromString(archiveName) ?: return@launchJob

        val modIds = CliParser.resolveSlugs(modSlugNames, archive) ?: return@launchJob

        val spinner = SpinnerAnimation()
        spinner.start()
        val removalResolveResult = archive.resolveRemoval(modIds, spinner::update)
        spinner.stop("resolved remove transaction")
        terminal.println()

        if (removalResolveResult.stillNeeded.isNotEmpty() || removalResolveResult.notInstalled.isNotEmpty()) {
            terminal.println("The following content cannot be uninstalled:")
            removalResolveResult.stillNeeded.forEach {
                terminal.println("  ${it.optimalTerminalString()} ${TextColors.brightCyan("(required as a dependency)")}")
            }
            removalResolveResult.notInstalled.forEach {
                terminal.println("  ${it.optimalTerminalString()} ${TextColors.brightYellow("(not installed)")}")
            }
            terminal.println()
        }

        if (removalResolveResult.transaction.isEmpty()) {
            terminal.println("All of the given mods ${TextColors.brightRed("cannot be removed")}")
            return@launchJob
        }

        val modStrings = removalResolveResult.transaction.resolveModStrings()

        if (
            !terminal.printAndConfirmTransaction(
                "Removing the given mods will result in the following transaction:",
                removalResolveResult.transaction,
                modStrings,
                yesFlag
            )
        ) return@launchJob

        terminal.handleTransaction(archive, removalResolveResult.transaction, modStrings)
    }
}
