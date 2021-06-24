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
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.first
import kotlinx.dnq.query.toList
import net.axay.pacmc.commands.Install.findBestFile
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.requests.data.CurseProxyFile
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.storage.Xodus.xodus
import net.axay.pacmc.storage.data.PacmcFile
import net.axay.pacmc.storage.data.XdArchive
import net.axay.pacmc.storage.data.XdMod
import net.axay.pacmc.terminal
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object Update : CliktCommand(
    "Updates the mods in an archive"
) {
    private val archiveName by option("-a", "--archive").default(".minecraft")

    private class UpdateMod(val xdMod: XdMod, val id: String, val name: String)

    override fun run() = runBlocking(Dispatchers.Default) {
        val (archivePath, minecraftVersion) = Xodus.getArchiveData(archiveName) ?: return@runBlocking

        terminal.println("Checking for updates for the mods at ${gray(archivePath)}")
        terminal.println()

        val upToDateCounter = AtomicInteger(0)
        val updateCounter = AtomicInteger(0)
        val unsureCounter = AtomicInteger(0)

        val allMods = xodus { XdArchive.filter { it.name eq archiveName }.first().mods }

        val mods = xodus {
            allMods.filter { it.persistent eq true }.toList().map { UpdateMod(it, it.id, it.name) }
        }

        val dependencyIds = xodus {
            allMods.filter { it.persistent eq false }.toList().map { it.id }
        }

        val archiveFolder = File(archivePath)
        val archiveFiles = (archiveFolder.listFiles() ?: emptyArray())
            .filter { it.name.startsWith("pacmc_") }
            .map { it to PacmcFile(it.name) }

        val updateMods = Collections.synchronizedList(ArrayList<Pair<UpdateMod, CurseProxyFile>>())
        val freshDependencies = Collections.synchronizedList(ArrayList<Install.ResolvedDependency>())

        mods.map { mod ->
            launch {
                val modFile = CurseProxy.getModFiles(mod.id.toInt())?.findBestFile(minecraftVersion)?.first
                if (modFile == null) {
                    terminal.danger("Could not check the following mod: ${mod.name} (has it been deleted by its owner?)")
                    unsureCounter.incrementAndGet()
                } else {
                    freshDependencies += Install.findDependencies(modFile, minecraftVersion)
                        .filterNot { dep -> freshDependencies.any { it.addonId == dep.addonId } }

                    xodus {
                        if (modFile.id.toString() != mod.xdMod.version) {
                            terminal.println("The mod ${bold("${mod.xdMod.repository}/${underline(mod.name)}")} is ${red("outdated")}")
                            updateMods += mod to modFile
                        } else {
                            terminal.println("The mod ${bold("${mod.xdMod.repository}/${underline(mod.name)}")} is up to date")
                            upToDateCounter.incrementAndGet()
                        }
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

        if (updateMods.isNotEmpty() || freshDependencies.isNotEmpty()) {
            terminal.println("Deleting old files...")
            archiveFiles.forEach {
                // delete files which will be updated or aren't needed anymore
                if (
                    it.second.modId in removableDependencies ||
                    freshDependencies.any { dep -> dep.addonId == it.second.modId } ||
                    updateMods.any { newMod -> newMod.first.id == it.second.modId }
                ) it.first.delete()
            }

            if (updateMods.isNotEmpty()) {
                terminal.println()
                terminal.println("Updating mods...")
                terminal.println()

                updateMods.forEach {
                    Install.downloadFile(it.first.id, it.second, archivePath, "curseforge", it.second.id.toString(), null, true)
                    updateCounter.incrementAndGet()
                }
            }

            if (freshDependencies.isNotEmpty()) {
                terminal.println()
                terminal.println("Updating dependencies...")
                terminal.println()

                freshDependencies.forEach {
                    Install.downloadFile(it.addonId, it.file, archivePath, "curseforge", it.file.id.toString(), it.info, false)
                    updateCounter.incrementAndGet()
                }
            }
        }

        terminal.println()
        terminal.println("Summary: $updateCounter updated, $upToDateCounter are up to date, ${removableDependencies.size} removed, $unsureCounter checks failed")
    }
}
