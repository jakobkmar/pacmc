package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import net.axay.pacmc.app.data.ModSlug
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.DownloadAnimation
import net.axay.pacmc.cli.terminal.askYesOrNo

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
        terminal.println("Resolving versions and dependencies...")

        val archive = Archive(archiveName!!)

        val resolveResult = archive.resolve(modSlugNames.map { ModSlug(Repository.MODRINTH, it) })
        terminal.println()

        terminal.println("Installing the following:")
        resolveResult.versions.forEach { version ->
            terminal.println(TextColors.brightGreen("  " + version.files.first().name.removeSuffix(".jar")))
        }

        terminal.println()
        terminal.println("Installing the following dependencies:")
        resolveResult.dependencyVersions.forEach { version ->
            terminal.println(TextColors.brightYellow("  " + version.files.first().name.removeSuffix(".jar")))
        }

        terminal.println()
        if (!terminal.askYesOrNo("Is this okay?", default = true)) {
            terminal.println("Abort.")
            return@launchJob
        }
        terminal.println()

        val downloadAnimation = DownloadAnimation()

        resolveResult.versions.forEach { version ->
            val fileName = version.files.first().name.removeSuffix(".jar")
            val installResult = archive.install(version, false) {
                downloadAnimation.update(fileName, DownloadAnimation.AnimationState(it))
            }

            if (installResult == Archive.InstallResult.SUCCESS) return@forEach

            val message = when (installResult) {
                Archive.InstallResult.ALREADY_INSTALLED -> TextColors.brightGreen("already installed")
                Archive.InstallResult.NO_PROJECT_INFO -> TextColors.brightRed("no project info")
                Archive.InstallResult.NO_FILE -> TextColors.brightRed("no downloadable file")
                else -> null
            }?.let { TextStyles.bold(it) }

            val color = if (!installResult.success) TextColors.brightRed else null

            downloadAnimation.update(fileName, DownloadAnimation.AnimationState(1.0, message, color))
        }
    }
}
