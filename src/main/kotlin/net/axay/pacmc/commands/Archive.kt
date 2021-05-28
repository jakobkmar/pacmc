package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.Values
import net.axay.pacmc.requests.CurseProxy
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

        override fun run() = runBlocking {
            val minecraftVersion = async {
                gameVersion ?: CurseProxy.getMinecraftVersions().first().versionString
            }
            Values.archiveStore.use { store ->
                store.executeInTransaction {
                    val existingArchive = it.find("Archive", "name", name).firstOrNull()
                    if (existingArchive != null) {
                        echo("An archive with the name \"$name\" already exists!")
                    } else {
                        val archive = it.newEntity("Archive")
                        archive.setProperty("name", name)
                        archive.setProperty("path", path.canonicalPath)
                        archive.setProperty("gameVersion", runBlocking { minecraftVersion.await() })
                    }
                }
            }
        }
    }

    object List : CliktCommand("Lists all archives you have created") {
        override fun run() {
            Values.archiveStore.use { store ->
                store.computeInTransaction {
                    val archives = it.getAll("Archive")
                    archives.forEachIndexed { index, archive ->
                        val isLast = index.toLong() == archives.size() - 1

                        val name = brightYellow(archive.getProperty("name").toString())
                        val gameVersion = cyan(archive.getProperty("gameVersion").toString())
                        val path = gray(archive.getProperty("path").toString())

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
            Values.archiveStore.use { store ->
                store.executeInTransaction { ta ->
                    val archive = ta.find("Archive", "name", name).first
                    if (archive == null) {
                        echo("There is no archive with the name \"$name\"")
                    } else {
                        val name = red(archive.getProperty("name").toString())
                        val path = gray(archive.getProperty("path").toString())

                        terminal.println("$name at $path")

                        var sure: Boolean? = null
                        while (sure == null) {
                            print("Do you really want to delete this archive? (y (yes) / n (no)) ")
                            sure = when (readLine()) {
                                "y", "yes" -> true
                                "n", "no", null -> false
                                else -> null
                            }
                        }

                        if (sure) {
                            archive.delete()
                            echo("Deleted archive \"$name\"")
                        } else {
                            echo("Deletion canceled.")
                        }
                    }
                }
            }
        }
    }
}
