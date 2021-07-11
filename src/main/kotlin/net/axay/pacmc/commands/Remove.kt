package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.data.PacmcFile
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import org.kodein.db.delete
import org.kodein.db.find
import org.kodein.db.keyById
import org.kodein.db.useEntries
import java.io.File

object Remove : CliktCommand(
    "Removes a minecraft mod"
) {
    private val archiveName by option("-a", "--archive", help = "The name of the archive where you want to remove the mod").default(".minecraft")

    private val inputModName by argument()

    override fun run() {
        val archive = db.getArchiveOrWarn(archiveName) ?: return

        val (maybeRepository, maybeModId) = inputModName.split('/')
            .let { if (it.size == 2) it else listOf(null, null) }

        val modEntry = db.find<DbMod>()
            .byIndex("archiveRepoIdIndex", listOf(maybeRepository, maybeModId, db.keyById<DbArchive>(archiveName)))
            .useEntries { it.firstOrNull() }
            ?: db.find<DbMod>()
                .byIndex("name", inputModName)
                .useEntries {
                    if (it.firstOrNull() != null) {
                        val mod = it.singleOrNull()
                        if (mod == null)
                            terminal.warning("There are multiple mods matching the given name, please specify the ID!")
                        mod
                    } else null
                }

        if (modEntry == null) {
            terminal.danger("No unique mod with the given name or ID was found!")
            return
        }

        val (modKey, mod) = modEntry

        terminal.println("Deleting all files of that mod...")
        (File(archive.path).listFiles() ?: emptyArray()).forEach {
            if (it.name.startsWith("pacmc_") && PacmcFile(it.name).modId == mod.modId) {
                it.delete()
            }
        }

        terminal.println("Deleting mod from archive list...")
        db.delete(modKey)

        terminal.success("Successfully deleted mod ${mod.name}")
    }
}
