package net.axay.pacmc.cli.terminal

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.defaultLazy
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.axay.pacmc.app.features.Archive

fun CliktCommand.archiveIdOption(
    help: String
) = option("-a", "--archive", help = help)
    .defaultLazy { Archive.getDefault() }

fun CliktCommand.archiveIdArgument(
    help: String
) = argument("archiveIdentifier", help = help)
    .defaultLazy { Archive.getDefault() }

fun CliktCommand.yesFlag() = option(
    "-y", "--yes",
    help = "When this flag is set, all user prompts will be automatically confirmed with yes"
).flag()
