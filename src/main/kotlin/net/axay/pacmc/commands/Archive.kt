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
import kotlinx.dnq.query.*
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.storage.data.XdArchive
import net.axay.pacmc.terminal

object Archive : CliktCommand(
    "Manage your mod archives"
) {
    init {
        subcommands(Add, List, Remove)
    }

    override fun run() = Unit

    object Add : CliktCommand("Add a new archive") {
        private val gameVersion by option("-g", "--game-version", help = "Set a specific game version (latest by default)")

        private val name by argument()
        private val path by argument().file(mustExist = true, canBeFile = false)

        override fun run() = runBlocking(Dispatchers.Default) {
            val minecraftVersion = async {
                gameVersion ?: CurseProxy.getMinecraftVersions().first().versionString
            }
            Xodus.store.transactional {
                if (XdArchive.query(XdArchive::name eq name).size() > 0) {
                    terminal.danger("An archive with the name '$name' already exists!")
                } else {
                    XdArchive.new {
                        name = this@Add.name
                        path = this@Add.path.canonicalPath
                        gameVersion = runBlocking { minecraftVersion.await() }
                    }
                    terminal.success("Successfully added the new archive '$name'")
                }
            }
        }
    }

    object List : CliktCommand("Lists all archives you have created") {
        override fun run() {
            Xodus.store.transactional {
                val archives = XdArchive.all().toList()
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
    }

    object Remove : CliktCommand("Remove an existing archive") {
        private val name by argument()

        override fun run() {
            Xodus.store.transactional {
                val archive = XdArchive.query(XdArchive::name eq name).firstOrNull()
                if (archive == null) {
                    terminal.danger("The given archive '$name' does not exist!")
                } else {
                    terminal.println("${red(archive.name)} at ${gray(archive.path)}")

                    var sure: Boolean? = null
                    while (sure == null) {
                        terminal.print("Do you really want to delete this archive? (${brightGreen("y")} (yes) / ${brightRed("n")} (no)) ")
                        sure = when (readLine()) {
                            "y", "yes" -> true
                            "n", "no", null -> false
                            else -> null
                        }
                    }

                    if (sure) {
                        archive.delete()
                        terminal.println("Deleted archive '${red(name)}'")
                    } else {
                        terminal.println("Deletion canceled.")
                    }
                }
            }
        }
    }
}
