package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.*

class InstallCommand : CliktCommand(
    name = "install",
    help = "Install content to an archive",
) {
    private val modSlugNames by argument(
        "mods",
        help = "The slugs of mods which should be installed, optionally prefixed with the repository"
    ).multiple()

    private val archiveName by archiveIdOption("The archive where the mods should be installed")

    private val yesFlag by yesFlag()

    override fun run() = launchJob {
        terminal.println("Resolving versions and dependencies...")
        val archive = Archive.terminalFromString(archiveName) ?: return@launchJob

        val modIds = CliParser.resolveSlugs(modSlugNames) ?: return@launchJob

        val spinner = SpinnerAnimation()
        spinner.start()
        val transaction = archive.resolve(modIds, spinner::update)
        spinner.stop("resolved install transaction")
        terminal.println()

        if (transaction.isEmpty()) {
            terminal.println(TextColors.brightRed("No files matching your archive version and loader were found"))
            return@launchJob
        }

        val modStrings = transaction.resolveModStrings()

        if (
            !terminal.printAndConfirmTransaction(
                "Installing the given mods will result in the following transaction:",
                transaction,
                modStrings,
                yesFlag
            )
        ) return@launchJob

        terminal.handleTransaction(archive, transaction, modStrings)
    }
}
