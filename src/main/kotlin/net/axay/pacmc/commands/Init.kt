package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors.gray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.dnq.query.eq
import kotlinx.dnq.query.firstOrNull
import kotlinx.dnq.query.query
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.storage.data.XdArchive
import net.axay.pacmc.terminal
import net.axay.pacmc.utils.OperatingSystem
import java.io.File

object Init : CliktCommand(
    "Sets the pacmc defaults"
) {
    override fun run() = runBlocking(Dispatchers.Default) {
        val latestMinecraftVersion = async {
            CurseProxy.getMinecraftVersions().first().versionString
        }

        // set the .minecraft archive
        Xodus.store.transactional {
            val existingArchive = XdArchive.query(XdArchive::name eq ".minecraft").firstOrNull()
            if (existingArchive != null) {
                terminal.println("The '.minecraft' archive already exists. (at ${gray(existingArchive.path)})")
            } else {
                terminal.println("The '.minecraft' archive does not exist.")

                val minecraftFolder = when (OperatingSystem.current) {
                    // get the .minecraft folder for the current operating system
                    OperatingSystem.LINUX -> File(System.getProperty("user.home"), "/.minecraft/")
                    OperatingSystem.WINDOWS -> File(File(System.getenv("%APPDATA%")), "/.minecraft/")
                    OperatingSystem.MACOS -> File(System.getProperty("user.home"), "/Library/Application Support/minecraft/")
                    // just use the linux path then
                    else -> File(System.getProperty("user.home"), "/.minecraft/")
                }
                val path = if (!minecraftFolder.exists()) {
                    terminal.danger("There is no .minecraft folder present at the default location!")
                    return@transactional
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

                XdArchive.new {
                    this.name = ".minecraft"
                    this.path = path
                    this.gameVersion = runBlocking { latestMinecraftVersion.await() }
                }

                terminal.success("Created the '.minecraft' archive at:")
                terminal.println("  " + gray(path))
            }
        }
    }
}
