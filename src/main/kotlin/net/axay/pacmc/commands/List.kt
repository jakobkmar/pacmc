package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.underline
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.firstOrNull
import kotlinx.dnq.query.toList
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.storage.data.XdArchive
import net.axay.pacmc.storage.data.XdMod
import net.axay.pacmc.terminal

object List : CliktCommand(
    "Lists the installed mods"
) {
    private val archiveName by option("-a", "--archive").default(".minecraft")
    private val muteDependencies by option("-m", "--mute").flag()

    override fun run() {
        Xodus.store.transactional {
            val archive = XdArchive.filter { it.name eq archiveName }.firstOrNull()
            if (archive == null) {
                terminal.danger("The given archive '${archiveName}' does not exist!")
                return@transactional
            }

            fun printMod(mod: XdMod, persistent: Boolean) {
                val name = white(bold(underline(mod.name)))
                val id = brightBlue("[${mod.repository}/${mod.id}]")

                val type = if (!persistent) " ${cyan("(dependency)")}" else ""

                terminal.println(" → $name $id$type")
                terminal.println("    ${gray(mod.description ?: "no description available")}")
            }

            archive.mods.apply {
                val installedMods = filter { it.persistent eq true }.toList()
                val installedDependencies = filter { it.persistent eq false }.toList()

                if (installedMods.isEmpty() && installedDependencies.isEmpty()) {
                    terminal.warning("The archive '$archiveName' is empty!")
                    return@transactional
                }

                terminal.println("The archive '${green(archiveName)}' at ${gray(archive.path)} contains the following mods:")
                terminal.println()
                installedMods.forEach { printMod(it, true) }

                if (!muteDependencies) {
                    terminal.println()
                    installedDependencies.forEach { printMod(it, false) }
                }
            }

        }
    }
}
