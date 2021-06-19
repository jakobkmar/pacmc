package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.gray
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextStyles.bold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.commands.Install.findBestFile
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.storage.data.PacmcFile
import net.axay.pacmc.terminal
import java.io.File

object Update : CliktCommand(
    "Updates the mods in an archive"
) {
    private val archiveName by option("-a", "--archive").default(".minecraft")

    override fun run() = runBlocking(Dispatchers.Default) {
        val transaction = Xodus.store.beginReadonlyTransaction()

        val archive = Xodus.getArchive(archiveName)
        if (archive == null) {
            terminal.danger("The given archive '${archiveName}' does not exist!")
            return@runBlocking
        }

        terminal.println("Checking for updates for the mods at ${gray(archive.path)}")
        terminal.println()

        val archiveFolder = File(archive.path)

        val modFiles = (archiveFolder.listFiles() ?: emptyArray())
            .filter { it.name.startsWith("pacmc_") }
            .map { PacmcFile(it.name) }

        var upToDateCounter = 0
        var updateCounter = 0
        var unsureCounter = 0

        modFiles.map {
            async {
                it to CurseProxy.getModFiles(it.modId.toInt())?.findBestFile(archive)?.first
            }
        }.forEach {
            val updateResult = it.await()
            val oldFile = updateResult.first
            val newFile = updateResult.second
            if (newFile != null) {
                if (newFile.id.toString() != oldFile.versionId) {
                    terminal.println("The mod ${bold("${oldFile.repository}/${oldFile.modId}")} is ${red("outdated")}")
                    File(archiveFolder, oldFile.filename).delete()
                    Install.downloadFile(oldFile.modId.toInt(), newFile, archive)
                    updateCounter++
                } else {
                    terminal.println("The mod ${bold("${oldFile.repository}/${oldFile.modId}")} is up to date")
                    upToDateCounter++
                }
            } else {
                terminal.println("Could not check the following file: ${oldFile.filename} (has the mod been deleted by its owner?)")
                unsureCounter++
            }
        }

        terminal.println()
        terminal.println("Summary: $updateCounter updated, $upToDateCounter are up to date, $unsureCounter checks failed")

        transaction.commit()
    }
}
