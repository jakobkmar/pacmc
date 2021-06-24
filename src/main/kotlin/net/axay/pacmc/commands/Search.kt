package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.logging.printProject
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.terminal

object Search : CliktCommand(
    "Searches for mods"
) {
    private val searchTerm by argument()
    private val gameVersion by option("-g", "--game-version", help = "Set a specific game version (latest by default)")
    private val allVersions by option("-i", "--all-versions", help = "Whether to show mods for all Minecraft versions").flag()
    private val allResults by option("-a", "--all", help = "Whether to show all results without any limit").flag()
    private val limit by option("-l", "--limit", help = "The amount of results (defaults to 15)").int().default(15)

    override fun run() = runBlocking(Dispatchers.Default) {
        val versionRequest = async {
            when {
                gameVersion != null -> gameVersion
                allVersions -> null
                else -> CurseProxy.getMinecraftVersions().first().versionString
            }
        }
        CurseProxy.search(
            searchTerm,
            null,
            if (!allResults) limit else null
        ).forEach {
            terminal.printProject(it, versionRequest.await(), allVersions)
        }
    }
}
