package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class PacmcCommand : CliktCommand(
    name = "pacmc",
    help = "The root command of pacmc",
) {
    init {
        subcommands(SearchCommand(), ArchiveCommand(), InstallCommand(), UpdateCommand())
    }

    override fun run() = Unit
}
