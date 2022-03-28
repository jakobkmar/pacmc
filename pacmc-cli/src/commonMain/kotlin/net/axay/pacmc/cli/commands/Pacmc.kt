package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class Pacmc : CliktCommand(
    "The root command of pacmc"
) {
    init {
        subcommands(Search())
    }

    override fun run() = Unit
}
