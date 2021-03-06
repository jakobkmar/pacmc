package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.white
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.axay.pacmc.commands.Install.findBestFile
import net.axay.pacmc.requests.common.RepositoryApi
import net.axay.pacmc.requests.common.data.CommonModInfo
import net.axay.pacmc.requests.common.data.CommonModVersion
import net.axay.pacmc.storage.data.DbArchive
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
        terminal.println("Refreshing mods for archive '${archiveName}'")
        terminal.println()

        refreshArchive(db.getArchiveOrWarn(archiveName) ?: return@runBlocking)
    }

    suspend fun refreshArchive(archive: DbArchive, changedVersion: Boolean = false) = coroutineScope {
        val mods = db.getArchiveMods(archive.name).filter { it.persistent }

        val freshFiles = Collections.synchronizedList(ArrayList<Triple<DbMod, CommonModVersion, Deferred<CommonModInfo>>>())
        val freshDependencies = Collections.synchronizedList(ArrayList<Install.ResolvedDependency>())

        terminal.println("Retrieving updated file data...")
        val warnLock = Mutex()
        mods.map { mod ->
            launch {
                val freshFile = RepositoryApi.getModVersions(mod.modId)?.findBestFile(archive.minecraftVersion)?.first
                if (freshFile == null) {
                    warnLock.withLock {
                        terminal.println()
                        if (changedVersion) {
                            terminal.danger("Could not find a release of ${white("${mod.repository}/${mod.name}")} for the new minecraft version!")
                            terminal.danger(" (you can switch back to the previous version to install this mod again)")
                        } else {
                            terminal.danger("Could not find the file for ${white("${mod.repository}/${mod.name}")}!")
                            terminal.danger(" Is it not available for this version? Did the author delete the mod?")
                        }
                    }
                    return@launch
                }
                freshFiles += Triple(mod, freshFile, async { RepositoryApi.getModInfo(mod.modId)!! })
                Install.findDependencies(freshFile, archive.minecraftVersion).forEach { dep ->
                    if (!freshDependencies.any { it.addonId == dep.addonId })
                        freshDependencies += dep
                }
            }
        }.joinAll()

        val oldFiles = archive.javaFiles
        if (oldFiles.isNotEmpty()) {
            terminal.println()
            val fileNames = oldFiles.joinToString { it.name }
            oldFiles.forEach { it.delete() }
            terminal.println("Deleted old files: $fileNames")
        }

        // remove resolved dependencies which are installed mods as well
        freshDependencies.removeIf { dep -> freshFiles.any { it.first.modId == dep.addonId } }

        if (freshFiles.isNotEmpty()) {
            terminal.println()
            terminal.println("Downloading mod files...")
            terminal.println()

            freshFiles.forEach {
                Install.downloadFile(it.first.modId, it.second, it.third, true, archive, false)
            }
        }

        if (freshDependencies.isNotEmpty()) {
            terminal.println()
            terminal.println("Downloading dependency files...")
            terminal.println()

            freshDependencies.forEach {
                Install.downloadFile(it.addonId, it.file, it.info, false, archive)
            }
        }

        if (freshFiles.size < mods.size) {
            terminal.println()
            terminal.danger("Some mods could not be refreshed! (more information above)")
        }
    }
}
