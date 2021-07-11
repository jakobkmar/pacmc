package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.logging.awaitConfirmation
import net.axay.pacmc.logging.printArchive
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.db
import net.axay.pacmc.terminal
import org.kodein.db.*

object Archive : CliktCommand(
    "Manages your mod archives"
) {
    init {
        subcommands(Add, List, Remove)
    }

    override fun run() = Unit

    object Add : CliktCommand("Add a new archive") {
        private val gameVersion by option("-g", "--game-version", help = "Set a specific game version (latest by default)")

        private val name by argument(help = "The name of the new archive")
        private val path by argument(help = "The path where all archive specific actions should take place").file(mustExist = true, canBeFile = false)

        override fun run() = runBlocking(Dispatchers.Default) {
            val minecraftVersion = async {
                gameVersion ?: CurseProxy.getMinecraftVersions().first().versionString
            }
            if (db.find<DbArchive>().byId(name).use { it.isValid() }) {
                terminal.danger("An archive with the name '$name' already exists!")
            } else {
                db.put(DbArchive(name, path.canonicalPath, runBlocking { minecraftVersion.await() }))
                terminal.success("Successfully added the new archive '$name'")
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

    object Remove : CliktCommand("Remove an existing archive") {
        private val name by argument(help = "The name of the archive you want to delete")

        override fun run() {
            val archive = db.getById<DbArchive>(name)
            if (archive == null) {
                terminal.danger("The given archive '$name' does not exist!")
            } else {
                terminal.printArchive(archive)

                terminal.print("Do you really want to delete this archive?")

                if (awaitConfirmation()) {
                    db.execBatch {
                        deleteById<DbArchive>(name)
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
}
