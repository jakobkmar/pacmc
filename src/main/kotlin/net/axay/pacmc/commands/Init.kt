package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.terminal
import net.axay.pacmc.utils.OperatingSystem
import java.io.File

object Init : CliktCommand(
    "Sets the pacmc defaults if they aren't already set"
) {
    override fun run() = runBlocking(Dispatchers.Default) {
        val latestMinecraftVersion = async {
            CurseProxy.getMinecraftVersions().first().versionString
        }

        // set the .minecraft archive
        Xodus.archiveStore.use { store ->
            store.executeInTransaction {
                val existingArchive = it.find("Archive", "name", ".minecraft").firstOrNull()
                if (existingArchive != null) {
                    echo("The \".minecraft\" archive already exists.")
                } else {
                    terminal.println("The \".minecraft\" archive does not exist.")

                    val minecraftFolder = when (OperatingSystem.current) {
                        // get the .minecraft folder for the current operating system
                        OperatingSystem.LINUX -> File(System.getProperty("user.home"), "/.minecraft/")
                        OperatingSystem.WINDOWS -> File(System.getenv("%APPDATA%"), "/.minecraft/")
                        OperatingSystem.MACOS -> File(System.getProperty("user.home"), "/Library/Application Support/minecraft/")
                        // just use the linux path then
                        else -> File(System.getProperty("user.home"), "/.minecraft/")
                    }
                    val path = if (!minecraftFolder.exists()) {
                        terminal.println("There is no .minecraft folder present at the default location!")
                        return@executeInTransaction
                    } else {
                        File(minecraftFolder, "/mods/").apply {
                            terminal.println("Found the .minecraft folder at the default location.")
                            // create the mods folder if it does not exist
                            if (!exists()) {
                                terminal.println("Creating the mods folder...")
                                mkdir()
                            }
                        }.canonicalPath
                    }

                    echo("Created the \".minecraft\" archive at:")
                    terminal.println("  " + TextColors.gray(path))
                    val archive = it.newEntity("Archive")
                    archive.setProperty("name", ".minecraft")
                    archive.setProperty("path", path)
                    archive.setProperty("gameVersion", runBlocking { latestMinecraftVersion.await() })
                }
            }
        }
    }
}
