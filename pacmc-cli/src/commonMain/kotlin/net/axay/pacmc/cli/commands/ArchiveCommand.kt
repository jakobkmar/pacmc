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
import okio.Path
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
        private val archiveName by argument("identifier", help = "The archive identifier")
        private val archivePathString by argument("path", help = "The path to the archive directory")

        private val gameVersionString by option("-g", "--game-version", help = "The game version content installed to this archive must be made for")
        private val archiveDisplayName by option("-d", "--display-name", help = "Display name for the archive allowing more characters than the identifier")
        private val loaderNameString by option("-l", "--load", help = "The loader mods installed to this archive must support")

        companion object {
            suspend fun create(
                name: String,
                displayName: String?,
                path: Path,
                gameVersion: MinecraftVersion?,
                loader: ModLoader?,
            ) {
                val actualGameVersionDeferred = CoroutineScope(Dispatchers.Default).async {
                    gameVersion ?: repoApiContext(CachePolicy.ONLY_FRESH) { it.getMinecraftReleases() }?.maxOrNull()
                }

                val archive = Archive(name)
                if (archive.exists()) {
                    terminal.warning("The archive '$name' already exists")
                    return
                }

                val actualGameVersion = actualGameVersionDeferred.await()
                if (actualGameVersion == null) {
                    terminal.danger("Failed to load most recent game version, specify one explicitly or try again later")
                    return
                }

                Archive.create(DbArchive(
                    name,
                    displayName ?: name,
                    path,
                    actualGameVersion,
                    ContentType.MOD,
                    listOf(loader ?: ModLoader.FABRIC),
                    emptyList(),
                    ColorUtils.randomLightColor().toRGBInt().argb.toInt()
                ))

                terminal.println()
                terminal.println("${TextColors.brightGreen("Successfully")} created the following archive:")
                terminal.println(archive)
            }
        }

        override fun run() = launchJob {
            val optionalGameVersion = gameVersionString?.let {
                MinecraftVersion.terminalFromString(it) ?: return@launchJob
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
            }

            create(
                archiveName,
                archiveDisplayName,
                archivePath,
                optionalGameVersion,
                loader
            )
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
            terminal.println("Resolving changes required for version change...")
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

    class Init : CliktCommand(
        name = "init",
        help = "Create default archives"
    ) {
        override fun run() = launchJob {
            terminal.println("Resolving possible archive locations...")
            val dotMinecraftArchive = Archive(".minecraft")
            if (dotMinecraftArchive.exists()) {
                terminal.warning("The archive '.minecraft' already exists:")
                terminal.println(dotMinecraftArchive)
                return@launchJob
            }

            val minecraftFolder = when (OperatingSystem.current) {
                OperatingSystem.LINUX -> Environment.userHome
                OperatingSystem.MACOS -> Environment.getEnv("APPDATA")!!.toPath()
                OperatingSystem.WINDOWS -> Environment.userHome.resolve("Library/Application Support/minecraft/")
                null -> {
                    terminal.warning("Unknown operating system, cannot proceed")
                    return@launchJob
                }
            }.resolve(".minecraft/")

            if (!Environment.fileSystem.exists(minecraftFolder)) {
                terminal.warning("There exists no .minecraft folder at the default location:")
                terminal.println("  ${TextColors.gray(minecraftFolder.toString())}")
                return@launchJob
            }

            val modsFolder = minecraftFolder.resolve("mods/")
            if (!Environment.fileSystem.exists(modsFolder)) {
                Environment.fileSystem.createDirectories(modsFolder)
                terminal.println("Created the 'mods' folder")
            }

            Create.create(
                ".minecraft", ".minecraft",
                modsFolder,
                null, null
            )
        }
    }
}
