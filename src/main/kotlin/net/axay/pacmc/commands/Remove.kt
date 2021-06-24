package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.singleOrNull
import kotlinx.dnq.query.size
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.storage.Xodus.xodus
import net.axay.pacmc.storage.data.PacmcFile
import net.axay.pacmc.terminal
import java.io.File

object Remove : CliktCommand(
    "Removes a minecraft mod"
) {
    private val archiveName by option("-a", "--archive").default(".minecraft")

    private val inputModName by argument()

    override fun run() = xodus {
        val archive = Xodus.getArchiveOrNull(archiveName) ?: return@xodus

        val mod = archive.mods.filter { it.id eq inputModName }.singleOrNull()
            ?: kotlin.run {
                val possibleMods = archive.mods.filter { it.name eq inputModName }
                if (possibleMods.size() > 1) {
                    terminal.warning("There are multiple mods matching the given name, please specify the ID")
                    null
                } else possibleMods.singleOrNull()
            }

        if (mod == null) {
            terminal.danger("You don't have any mod with the given name or ID installed.")
            return@xodus
        }

        val (modId, modName) = mod

        terminal.println("Deleting all files of that mod...")
        (File(archive.path).listFiles() ?: emptyArray()).forEach {
            if (it.name.startsWith("pacmc_") && PacmcFile(it.name).modId == modId) {
                it.delete()
            }
        }

        terminal.println("Deleting mod from archive list...")
        archive.mods.remove(mod)
        mod.delete()

        terminal.success("Successfully deleted mod $modName")
    }
}
