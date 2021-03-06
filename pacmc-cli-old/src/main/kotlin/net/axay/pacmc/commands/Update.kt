package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.gray
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextStyles.bold
import kotlinx.coroutines.*
import net.axay.pacmc.commands.Install.findBestFile
import net.axay.pacmc.requests.common.RepositoryApi
import net.axay.pacmc.requests.common.data.CommonModVersion
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.execAsyncBatch
import net.axay.pacmc.storage.getArchiveMods
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import org.kodein.db.delete
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

        val updateMods = Collections.synchronizedList(ArrayList<Pair<DbMod, CommonModVersion>>())
        val freshDependencies = Collections.synchronizedList(ArrayList<Install.ResolvedDependency>())

        mods.map { mod ->
            launch {
                val modFile = RepositoryApi.getModVersions(mod.modId)?.findBestFile(archive.minecraftVersion)?.first
                if (modFile == null) {
                    terminal.danger("The following mod could not be checked: ${mod.formattedName}")
                    unsureCounter.incrementAndGet()
                } else {
                    freshDependencies += Install.findDependencies(modFile, archive.minecraftVersion)
                        .filterNot { dep -> freshDependencies.any { it.addonId == dep.addonId } }

                    if (modFile.id != mod.version) {
                        terminal.println("The mod ${bold(mod.formattedName)} is ${red("outdated")}")
                        updateMods += mod to modFile
                    } else {
                        terminal.println("The mod ${bold(mod.formattedName)} is up to date")
                        upToDateCounter.incrementAndGet()
                    }
                }
            }
        }.joinAll()

        // figure out which installed dependencies aren't needed anymore
        val removableDependencies = dependencyIds.filter { depId -> !freshDependencies.any { it.addonId == depId } }

        val archiveFiles = archive.files

        // now remove all dependencies which don't have any update
        freshDependencies.removeIf { dep ->
            archiveFiles.any { it.second.modId == dep.addonId && it.second.versionId == dep.file.id }
        }

        terminal.println()
        terminal.println("Finished all update checks")

        if (updateMods.isNotEmpty() || freshDependencies.isNotEmpty() || removableDependencies.isNotEmpty()) {
            terminal.println("Deleting old files...")
            archiveFiles.forEach {
                // delete files which will be updated or aren't needed anymore
                if (
                    it.second.modId in removableDependencies ||
                    // may be removed later, as old files get removed by the downloadFile function anyways
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
                    Install.downloadFile(it.first.modId, it.second, CompletableDeferred(it.first.createModInfo()), true, archive, false)
                    updateCounter.incrementAndGet()
                }
            }

            if (freshDependencies.isNotEmpty()) {
                terminal.println()
                terminal.println("Updating dependencies...")
                terminal.println()

                freshDependencies.forEach {
                    Install.downloadFile(it.addonId, it.file, it.info, false, archive)
                    updateCounter.incrementAndGet()
                }
            }
        }

        terminal.println()
        terminal.println("Summary: $updateCounter updated, $upToDateCounter are up to date, ${removableDependencies.size} removed, $unsureCounter checks failed")
    }
}
