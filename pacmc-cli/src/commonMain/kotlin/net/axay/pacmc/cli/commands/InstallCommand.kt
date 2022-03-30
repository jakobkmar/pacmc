package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.data.ModSlug
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal

class InstallCommand : CliktCommand(
    name = "install",
    help = "Install a mod",
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
        val result = Archive(archiveName!!).resolve(modSlugNames.map { ModSlug(Repository.MODRINTH, it) })

        terminal.println("Installing the following:")
        result.versions.forEach { version ->
            terminal.println(TextColors.brightGreen("  " + version.files.first().name))
        }

        terminal.println()
        terminal.println("Installing the following dependencies:")
        result.dependencyVersions.forEach { version ->
            terminal.println(TextColors.brightYellow("  " + version.files.first().name))
        }
    }
}
