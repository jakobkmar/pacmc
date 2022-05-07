package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import net.axay.pacmc.app.data.ModSlug
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.SpinnerAnimation
import net.axay.pacmc.cli.terminal.handleTransaction
import net.axay.pacmc.cli.terminal.printAndConfirmTransaction
import net.axay.pacmc.cli.terminal.resolveModStrings

class InstallCommand : CliktCommand(
    name = "install",
    help = "Install content to an archive",
) {
    private val modSlugNames by argument(
        "mods",
        help = "The slugs of mods which should be installed, optionally prefixed with the repository"
    ).multiple()

    private val archiveName by option(
        "-a", "--archive",
        help = "The archive where the mods should be installed"
    )

    override fun run() = launchJob {
        terminal.println("Resolving versions and dependencies...")

        val archive = Archive(archiveName!!)

        val spinner = SpinnerAnimation()
        spinner.start()
        val transaction = archive.resolve(modSlugNames.mapTo(mutableSetOf()) { ModSlug(Repository.MODRINTH, it) }, spinner::update)
        spinner.stop()
        terminal.println()

        val modStrings = transaction.resolveModStrings()

        if (
            !terminal.printAndConfirmTransaction(
                "Installing the given mods will result in the following transaction:",
                transaction,
                modStrings
            )
        ) return@launchJob

        terminal.handleTransaction(archive, transaction, modStrings)
    }
}
