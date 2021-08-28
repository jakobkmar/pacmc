package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

object Pacmc : CliktCommand(
    "The root command of pacmc"
) {
    init {
        subcommands(Install, Update, Search, List, Remove, Archive, Init, Refresh, Load, Info, Debug)
    }

    override fun run() = Unit
}
