package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.underline
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.getArchiveMods
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import net.axay.pacmc.utils.OperatingSystem

object List : CliktCommand(
    "Lists the installed mods"
) {
    private val archiveName by option("-a", "--archive", help = "The name of the archive you want to list the mods of").default(".minecraft")
    private val muteDependencies by option("-m", "--mute", help = "Whether to list mods which are dependencies of other mods").flag()

    override fun run() {
        val archive = db.getArchiveOrWarn(archiveName) ?: return
        val mods = db.getArchiveMods(archiveName)

        fun printMod(mod: DbMod, persistent: Boolean) {
            val name = white(bold(underline(mod.name)))
            val id = brightBlue("[${mod.repository}/${mod.modId}]")

            val type = if (!persistent) " ${cyan("(dependency)")}" else ""

            // yikes, what are the Windows terminals even doing
            val arrowSymbol = if (OperatingSystem.current == OperatingSystem.WINDOWS) "->" else "→"

            terminal.println(" $arrowSymbol $name $id$type")
            terminal.println("    ${gray(mod.description ?: "no description available")}")
        }

        mods.apply {
            val installedMods = filter { it.persistent }
            val installedDependencies = filter { !it.persistent }

            if (installedMods.isEmpty() && installedDependencies.isEmpty()) {
                terminal.warning("The archive '$archiveName' is empty!")
                return
            }

            terminal.println("The archive '${green(archiveName)}' at ${gray(archive.path)} contains the following mods:")

            if (installedMods.isNotEmpty()) {
                terminal.println()
                installedMods.forEach { printMod(it, true) }
            }

            if (!muteDependencies && installedDependencies.isNotEmpty()) {
                terminal.println()
                installedDependencies.forEach { printMod(it, false) }
            }
        }
    }
}
