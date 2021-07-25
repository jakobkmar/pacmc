package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.Values
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.logging.awaitConfirmation
import net.axay.pacmc.logging.awaitContinueAnyways
import net.axay.pacmc.logging.printArchive
import net.axay.pacmc.requests.curse.CurseProxy
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import org.kodein.db.*
import java.io.File

object Archive : CliktCommand(
    "Manages your mod archives"
) {
    init {
        subcommands(Add, List, Remove, Version, Update)
    }

    override fun run() = Unit

    object Add : CliktCommand("Adds a new archive") {
        private val gameVersion by option("-g", "--game-version", help = "Set a specific game version (latest by default)")

        private val name by argument(help = "The name of the new archive")
        private val path by argument(help = "The path where all archive specific actions should take place")
            .file(mustExist = true, canBeFile = false)
            .defaultLazy {
                File(Values.dataLocalDir, "/archives/$name").apply { mkdirs() }
            }

        override fun run() = runBlocking(Dispatchers.Default) {
            val minecraftVersion = async {
                gameVersion ?: CurseProxy.getMinecraftVersions().first().versionString
            }
            when {
                db.find<DbArchive>().byId(name).use { it.isValid() } ->
                    terminal.danger("An archive with the name '$name' already exists!")
                db.find<DbArchive>().byIndex("path", path.canonicalPath).use { it.isValid() } ->
                    terminal.danger("Another archive is already present at that location!")
                else -> {
                    val archive = DbArchive(name, path.canonicalPath, runBlocking { minecraftVersion.await() })
                    terminal.println("Will add the following archive to the database:")
                    terminal.printArchive(archive)
                    db.put(archive)
                    terminal.println()
                    terminal.success("Successfully added the new archive '$name'")
                }
            }
        }
    }

    object List : CliktCommand("Lists all archives you have created") {
        override fun run() {
            val archives = db.find<DbArchive>().all().use { it.asModelSequence().toList() }
            if (archives.isEmpty()) {
                terminal.warning("You do not have any archives defined on this machine.")
            } else {
                archives.forEachIndexed { index, archive ->
                    val isLast = index == archives.lastIndex

                    val name = brightYellow(archive.name)
                    val gameVersion = cyan(archive.gameVersion)
                    val path = gray(archive.path)

                    terminal.println("""
                            ${if (isLast) '└' else '├'}── $name ($gameVersion)
                            ${if (isLast) ' ' else '│'}     $path
                        """.trimIndent())
                }
            }
        }
    }

    object Remove : CliktCommand("Removes an existing archive") {
        private val name by argument(help = "The name of the archive you want to delete")

        override fun run() {
            val archive = db.getById<DbArchive>(name)
            if (archive == null) {
                terminal.danger("The given archive '$name' does not exist!")
            } else {
                terminal.printArchive(archive)

                terminal.print("Do you really want to delete this archive?")

                if (terminal.awaitConfirmation()) {
                    db.execBatch {
                        // TODO: do it this way until https://github.com/Kodein-Framework/Kodein-DB/pull/38 is merged
                        delete(keyById<DbArchive>(name))
                        db.find<DbMod>().byIndex("archive", name).useKeys { modSequence ->
                            modSequence.forEach {
                                delete(it)
                            }
                        }
                    }
                    terminal.println("Deleted archive '${red(name)}'")
                } else {
                    terminal.println("Deletion canceled.")
                }

                // TODO: remove the mods on the hard drive
            }
        }
    }

    object Version : CliktCommand("Changes the version of an archive") {
        private val version by argument(help = "The new minecraft version you want to migrate the archive to")
        private val name by argument(help = "The name of the archive you want to change the version of").default(".minecraft")

        override fun run() = runBlocking(Dispatchers.Default) {
            terminal.println("Trying to change the version of the archive '$name' to '$version'")
            terminal.println()

            val archive = db.getArchiveOrWarn(name) ?: return@runBlocking

            if (MinecraftVersion.fromString(version) == null) {
                terminal.danger("The given minecraft version '$version' follows an invalid format!")
                return@runBlocking
            }

            if (!CurseProxy.getMinecraftVersions().any { it.versionString == version }) {
                terminal.warning("The given minecraft version '$version' is probably invalid!")
                if (!terminal.awaitContinueAnyways()) return@runBlocking
            }

            changeVersion(archive, version)
        }

        suspend fun changeVersion(archive: DbArchive, version: String) = coroutineScope {
            if (archive.gameVersion == version) {
                terminal.println("That version is already the current minecraft version of the archive!")
                return@coroutineScope
            }

            val newArchive = archive.copy(gameVersion = version)
            db.put(newArchive)
            terminal.println("Changed the version in the database")

            terminal.println("Redownloading all mods in the archive for the new version...")
            terminal.println()
            Refresh.refreshArchive(newArchive, true)

            terminal.println()
            terminal.success("Changed the version of the archive '${archive.name}' to '$version'")
        }
    }

    object Update : CliktCommand("Updates an archive to the latest version") {
        private val name by argument(help = "The name of the archive you want to change the version of").default(".minecraft")

        override fun run() = runBlocking(Dispatchers.Default) {
            val latestMinecraftVersion = async {
                CurseProxy.getMinecraftVersions().first().versionString.apply {
                    terminal.println("The latest version is '$this'")
                    terminal.println()
                }
            }

            terminal.println("Trying to change the version of the archive '$name' to the latest minecraft version")
            terminal.println()

            val archive = db.getArchiveOrWarn(name) ?: return@runBlocking

            Version.changeVersion(archive, latestMinecraftVersion.await())
        }
    }
}
