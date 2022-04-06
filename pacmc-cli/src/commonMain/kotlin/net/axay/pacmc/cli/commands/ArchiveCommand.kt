package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.colormath.model.RGBInt
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.app.repoapi.RepositoryApi
import net.axay.pacmc.app.utils.ColorUtils
import net.axay.pacmc.app.utils.OperatingSystem
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import okio.Path.Companion.toPath

class ArchiveCommand : CliktCommand(
    name = "archive",
    help = "Manage archives",
) {
    init {
        subcommands(Create(), List())
    }

    override fun run() = Unit

    class Create : CliktCommand(
        name = "create",
        help = "Create an archive",
    ) {
        private val archiveIdentifier by argument("identifier", help = "The archive identifier")
        private val archivePathString by argument("path", help = "The path to the archive directory")

        private val gameVersionString by option("-g", "--game-version", help = "The game version content installed to this archive must be made for")
        private val archiveDisplayName by option("-d", "--display-name", help = "Display name for the archive allowing more characters than the identifier")
        private val loaderNameString by option("-l", "--load", help = "The loader mods installed to this archive must support")

        override fun run() = launchJob {
            val optionalMinecraftVersion = gameVersionString?.let {
                MinecraftVersion.fromString(it) ?: kotlin.run {
                    terminal.warning("The given game version '$gameVersionString' is invalid!")
                    return@launchJob
                }
            }

            val minecraftVersionDeferred = CoroutineScope(Dispatchers.Default).async {
                optionalMinecraftVersion ?: RepositoryApi.getMinecraftReleases()?.maxOrNull()
            }

            val archivePath = try {
                archivePathString.toPath()
            } catch (exc: Exception) {
                terminal.warning("The given path '$archivePathString' is invalid! Problem: ${exc.message}")
                return@launchJob
            }

            if (!Environment.fileSystem.exists(archivePath) || !Environment.fileSystem.metadata(archivePath).isDirectory) {
                terminal.warning("The given path '$archivePathString' is not a valid directory!")
                return@launchJob
            }

            val loader = loaderNameString?.uppercase()?.let {
                kotlin.runCatching {
                    ModLoader.valueOf(it)
                }.onFailure {
                    terminal.warning("The given loader '$loaderNameString' is not supported!")
                    return@launchJob
                }.getOrThrow()
            } ?: ModLoader.FABRIC

            val minecraftVersion = minecraftVersionDeferred.await() ?: kotlin.run {
                terminal.warning("Could not resolve the latest game version!")
                return@launchJob
            }

            Archive.create(DbArchive(
                archiveIdentifier,
                archiveDisplayName ?: archiveIdentifier,
                archivePath,
                minecraftVersion,
                loader,
                emptyList(),
                ColorUtils.randomLightColor().toRGBInt().argb.toInt()
            ))
        }
    }

    class List : CliktCommand(
        name = "list",
        help = "List all archives",
    ) {
        override fun run() {
            val archives = Archive.getArchivesList()
            archives.forEachIndexed { index, archive ->
                val isLast = index == archives.lastIndex

                val color = TextColors.color(RGBInt(archive.color.toUInt()))
                val coloredChar = if (OperatingSystem.notWindows) "●" else TextStyles.bold(">")

                terminal.print("${if (isLast) '└' else '├'}─${color(coloredChar)}")
                terminal.print(" " + TextColors.brightWhite(TextStyles.bold(archive.name)))
                terminal.print(" " + TextColors.brightCyan(TextStyles.bold(archive.minecraftVersion)))
                if (archive.name != archive.displayName) {
                    terminal.print(" (${archive.displayName})")
                }
                terminal.println()
                terminal.println("${if (isLast) ' ' else '│'}     ${TextColors.gray(archive.path)}")
            }
        }
    }
}
