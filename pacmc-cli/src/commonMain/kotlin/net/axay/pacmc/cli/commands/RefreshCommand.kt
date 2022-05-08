package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.*

class RefreshCommand : CliktCommand(
    name = "refresh",
    help = "Refresh an archive and all content installed to it",
) {
    private val archiveName by archiveIdOption("The archive which should be refreshed")

    override fun run() = launchJob {
        terminal.println("Gather fresh data for all content inside '$archiveName'...")
        val archive = Archive.fromString(archiveName) ?: return@launchJob

        val spinner = SpinnerAnimation()
        spinner.start()
        val transaction = archive.resolve(archive.getInstalled().filterNot { it.dependency }.mapTo(HashSet()) { it.readModId() }, spinner::update)
        spinner.stop()
        terminal.println()

        terminal.println("Ready to refresh the archive.")
        terminal.danger("If you continue, all content installed to the archive will be deleted and a")
        terminal.danger("fresh copy will be downloaded over the network from the repository.")

        println()
        if (!terminal.askYesOrNo("Is this okay?", default = false)) {
            println("Abort.")
            return@launchJob
        }
        println()

        terminal.println("Deleting old files and clearing dependencies...")
        archive.prepareRefresh()
        terminal.println()

        terminal.handleTransaction(archive, transaction, transaction.resolveModStrings())
    }
}
