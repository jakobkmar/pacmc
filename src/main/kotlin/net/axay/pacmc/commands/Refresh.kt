package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.*
import net.axay.pacmc.commands.Install.findBestFile
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.requests.data.CurseProxyFile
import net.axay.pacmc.requests.data.CurseProxyProjectInfo
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.getArchiveMods
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import java.util.*

object Refresh : CliktCommand(
    "Refreshes the local mod files according to the database"
) {
    private val archiveName by option("-a", "--archive", help = "The name of the archive you want to refresh").default(".minecraft")

    override fun run() = runBlocking(Dispatchers.Default) {
        terminal.println("Refreshing mods for archive '$archiveName'")
        terminal.println()

        val archive = db.getArchiveOrWarn(archiveName) ?: return@runBlocking

        val mods = db.getArchiveMods(archiveName).filter { it.persistent }

        val freshFiles = Collections.synchronizedList(ArrayList<Triple<DbMod, CurseProxyFile, Deferred<CurseProxyProjectInfo>>>())
        val freshDependencies = Collections.synchronizedList(ArrayList<Install.ResolvedDependency>())

        val refreshJobs = mods.map { mod ->
            launch {
                val freshFile = CurseProxy.getModFiles(mod.modId.toInt())?.findBestFile(archive.minecraftVersion)?.first ?: return@launch
                freshFiles += Triple(mod, freshFile, async { CurseProxy.getModInfo(mod.modId.toInt()) })
                Install.findDependencies(freshFile, archive.minecraftVersion).forEach { dep ->
                    if (!freshDependencies.any { it.addonId == dep.addonId })
                        freshDependencies += dep
                }
            }
        }

        val oldFiles = archive.files
        terminal.println("Deleting old files")
        if (oldFiles.isNotEmpty()) {
            terminal.println()
            oldFiles.forEach {
                val name = it.name
                it.delete()
                terminal.println("Deleted $name")
            }
            terminal.println()
        }

        terminal.println("Retrieving updated file data...")
        refreshJobs.joinAll()

        // remove resolved dependencies which are installed mods as well
        freshDependencies.removeIf { dep -> freshFiles.any { it.first.modId == dep.addonId } }

        if (freshFiles.isNotEmpty()) {
            terminal.println()
            terminal.println("Downloading mod files...")
            terminal.println()

            freshFiles.forEach {
                Install.downloadFile(it.first.modId, it.second, it.first.repository, it.second.id.toString(), it.third, true, archive)
            }
        }

        if (freshDependencies.isNotEmpty()) {
            terminal.println()
            terminal.println("Downloading dependency files...")
            terminal.println()

            freshDependencies.forEach {
                Install.downloadFile(it.addonId, it.file, "curseforge", it.file.id.toString(), it.info, false, archive)
            }
        }
    }
}
