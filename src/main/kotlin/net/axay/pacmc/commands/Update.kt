package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.gray
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.underline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.commands.Install.findBestFile
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.requests.data.CurseProxyFile
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.data.PacmcFile
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.execAsyncBatch
import net.axay.pacmc.storage.getArchiveMods
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import org.kodein.db.delete
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object Update : CliktCommand(
    "Updates the mods inside an archive"
) {
    private val archiveName by option("-a", "--archive", help = "The name of the archive you want to update the mods of").default(".minecraft")

    override fun run() = runBlocking(Dispatchers.Default) {
        terminal.println("Checking for mod updates for the '$archiveName' archive")

        val archive = db.getArchiveOrWarn(archiveName) ?: return@runBlocking

        terminal.println("Will update the mods at ${gray(archive.path)}")
        terminal.println()

        val upToDateCounter = AtomicInteger(0)
        val updateCounter = AtomicInteger(0)
        val unsureCounter = AtomicInteger(0)

        val allMods = db.getArchiveMods(archiveName)

        val mods = allMods.filter { it.persistent }

        val dependencies = allMods.filter { !it.persistent }
        val dependencyIds = dependencies.map { it.modId }

        val archiveFolder = File(archive.path)
        val archiveFiles = (archiveFolder.listFiles() ?: emptyArray())
            .filter { it.name.startsWith("pacmc_") }
            .map { it to PacmcFile(it.name) }

        val updateMods = Collections.synchronizedList(ArrayList<Pair<DbMod, CurseProxyFile>>())
        val freshDependencies = Collections.synchronizedList(ArrayList<Install.ResolvedDependency>())

        mods.map { mod ->
            launch {
                val modFile = CurseProxy.getModFiles(mod.modId.toInt())?.findBestFile(archive.minecraftVersion)?.first
                if (modFile == null) {
                    terminal.danger("Could not check the following mod: ${mod.name} (has it been deleted by its owner?)")
                    unsureCounter.incrementAndGet()
                } else {
                    freshDependencies += Install.findDependencies(modFile, archive.minecraftVersion)
                        .filterNot { dep -> freshDependencies.any { it.addonId == dep.addonId } }

                    if (modFile.id.toString() != mod.version) {
                        terminal.println("The mod ${bold("${mod.repository}/${underline(mod.name)}")} is ${red("outdated")}")
                        updateMods += mod to modFile
                    } else {
                        terminal.println("The mod ${bold("${mod.repository}/${underline(mod.name)}")} is up to date")
                        upToDateCounter.incrementAndGet()
                    }
                }
            }
        }.joinAll()

        // figure out which installed dependencies aren't needed anymore
        val removableDependencies = dependencyIds.filter { depId -> !freshDependencies.any { it.addonId == depId } }

        // now remove all dependencies which don't have any update
        freshDependencies.removeIf { dep ->
            archiveFiles.any { it.second.modId == dep.addonId && it.second.versionId == dep.file.id.toString() }
        }

        terminal.println()
        terminal.println("Finished all update checks")

        if (updateMods.isNotEmpty() || freshDependencies.isNotEmpty() || removableDependencies.isNotEmpty()) {
            terminal.println("Deleting old files...")
            archiveFiles.forEach {
                // delete files which will be updated or aren't needed anymore
                if (
                    it.second.modId in removableDependencies ||
                    freshDependencies.any { dep -> dep.addonId == it.second.modId } ||
                    updateMods.any { newMod -> newMod.first.modId == it.second.modId }
                ) it.first.delete()
            }

            val removableMods = dependencies.filter { it.modId in removableDependencies }
            if (removableMods.isNotEmpty()) {
                removableMods.forEach {
                    terminal.println("Removing the dependency ${red(it.name)} because it is no longer needed")
                }
                db.execAsyncBatch {
                    for (removableDependency in removableMods) {
                        delete(db.keyFrom(removableDependency))
                    }
                }
            }

            if (updateMods.isNotEmpty()) {
                terminal.println()
                terminal.println("Updating mods...")
                terminal.println()

                updateMods.forEach {
                    Install.downloadFile(it.first.modId, it.second, archive.path, "curseforge", it.second.id.toString(), null, true, archive)
                    updateCounter.incrementAndGet()
                }
            }

            if (freshDependencies.isNotEmpty()) {
                terminal.println()
                terminal.println("Updating dependencies...")
                terminal.println()

                freshDependencies.forEach {
                    Install.downloadFile(it.addonId, it.file, archive.path, "curseforge", it.file.id.toString(), it.info, false, archive)
                    updateCounter.incrementAndGet()
                }
            }
        }

        terminal.println()
        terminal.println("Summary: $updateCounter updated, $upToDateCounter are up to date, ${removableDependencies.size} removed, $unsureCounter checks failed")
    }
}
