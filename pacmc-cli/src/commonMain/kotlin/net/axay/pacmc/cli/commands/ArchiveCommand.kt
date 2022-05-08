package net.axay.pacmc.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.colormath.model.RGBInt
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.data.ContentType
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.app.repoapi.repoApiContext
import net.axay.pacmc.app.utils.ColorUtils
import net.axay.pacmc.app.utils.OperatingSystem
import net.axay.pacmc.cli.launchJob
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.cli.terminal.*
import net.axay.pacmc.repoapi.CachePolicy
import okio.Path.Companion.toPath

class ArchiveCommand : CliktCommand(
    name = "archive",
    help = "Manage archives",
) {
    init {
        subcommands(Create(), List(), Remove(), SetDefault(), Version())
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
                MinecraftVersion.terminalFromString(it) ?: return@launchJob
            }

            val minecraftVersionDeferred = CoroutineScope(Dispatchers.Default).async {
                optionalMinecraftVersion ?: repoApiContext(CachePolicy.ONLY_FRESH) { it.getMinecraftReleases() }?.maxOrNull()
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
                ContentType.MOD,
                listOf(loader),
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

    class Remove : CliktCommand(
        name = "remove",
        help = "Remove an archive",
    ) {
        private val archiveName by archiveIdArgument("The archive which should be deleted")

        private val keepFiles by option(
            "-k", "--keep-files",
            help = "Whether the files installed to the archive should be kept"
        ).flag()

        override fun run() = launchJob {
            terminal.println("Finding archive '$archiveName'...")
            val archive = Archive.terminalFromString(archiveName) ?: return@launchJob

            terminal.println()
            terminal.println("The following archive will be removed:")
            terminal.println(archive.terminalString())
            if (!keepFiles) {
                terminal.println()
                terminal.println("Additionally, all content installed by pacmc to this archive will be")
                terminal.println("deleted, run this command with --keep-files to change this behaviour.")
            }

            terminal.println()
            if (!terminal.askYesOrNo("Do you want to continue?")) {
                terminal.println("Abort.")
                return@launchJob
            }
            terminal.println()

            archive.delete(keepFiles)

            terminal.println("${TextColors.brightRed("Removed")} archive '$archiveName'")
        }
    }

    class SetDefault : CliktCommand(
        name = "set-default",
        help = "Set the default archive",
    ) {
        private val archiveName by argument(
            name = "archiveIdentifier",
            help = "The archive which should be used by default for all commands"
        )

        override fun run() = launchJob {
            terminal.println("Setting default archive to '$archiveName'...")

            Archive.terminalFromString(archiveName) ?: return@launchJob
            Archive.setDefault(archiveName)

            terminal.println()
            terminal.println("${TextColors.brightGreen("Successfully")} set default archive to '$archiveName'")
        }
    }

    class Version : CliktCommand(
        name = "version",
        help = "Set the game version of an archive",
    ) {
        private val archiveName by archiveIdArgument("The archive whose game version should be changed")

        private val gameVersionString by option(
            "-g", "--game-version",
            help = "The game version content installed to this archive must be made for"
        ).required()

        override fun run() = launchJob {
            val archive = Archive.terminalFromString(archiveName) ?: return@launchJob
            val gameVersion = MinecraftVersion.terminalFromString(gameVersionString) ?: return@launchJob

            val previousGameVersion = archive.getGameVersion()

            if (gameVersion == previousGameVersion) {
                terminal.warning("The game version of '$archiveName' is already set to $previousGameVersion")
                return@launchJob
            }

            val spinner = SpinnerAnimation()
            spinner.start()
            val transaction = archive.setGameVersionAndResolveUpdate(gameVersion, spinner::update)
            spinner.stop()

            if (!transaction.isEmpty()) {
                terminal.println()

                val modStrings = transaction.resolveModStrings()

                if (
                    !terminal.printAndConfirmTransaction(
                        "Changing the game version to $gameVersion will result in the following transaction:",
                        transaction,
                        modStrings
                    )
                ) {
                    archive.setGameVersion(previousGameVersion)
                    return@launchJob
                }

                terminal.handleTransaction(archive, transaction, modStrings)
            }

            terminal.println()
            terminal.println("${TextColors.brightGreen("Successfully")} changed the game version of '$archiveName' to $gameVersion")
        }
    }
        }
    }
}
