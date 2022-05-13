package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import net.axay.pacmc.cli.terminal

class DebugCommand : CliktCommand(
    name = "debug",
    help = "Debug the pacmc application",
) {
    init {
        subcommands(Test())
    }

    override fun run() = Unit

    class Test : CliktCommand(
        name = "test",
        help = "Prints a stable test output",
    ) {
        override fun run() {
            terminal.println("Hello user! pacmc works.")
        }
    }
}
