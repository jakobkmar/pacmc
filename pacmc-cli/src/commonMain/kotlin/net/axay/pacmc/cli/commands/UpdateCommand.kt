package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.*

class UpdateCommand : CliktCommand(
    name = "update",
    help = "Update content installed to an archive",
) {
    private val archiveName by archiveIdOption("The archive which contains the content that should be updated")

    override fun run() = launchJob {
        terminal.println("Resolving updates for the given archive '$archiveName'...")
        val archive = Archive.terminalFromString(archiveName) ?: return@launchJob

        val spinner = SpinnerAnimation()
        spinner.start()
        val transaction = archive.resolveUpdate(spinner::update)
        spinner.stop()
        terminal.println()

        if (transaction.isEmpty()) {
            terminal.println("Everything is ${TextColors.brightGreen("up-to-date")}")
            return@launchJob
        }

        val modStrings = transaction.resolveModStrings()

        if (
            !terminal.printAndConfirmTransaction(
                "Updating the archive will result in the following transaction:",
                transaction,
                modStrings
            )
        ) return@launchJob

        terminal.handleTransaction(archive, transaction, modStrings)
    }
}
