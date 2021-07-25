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
import net.axay.pacmc.requests.curse.CurseProxy
import net.axay.pacmc.terminal

object Search : CliktCommand(
    "Searches for mods"
) {
    private val searchTerm by argument()
    private val gameVersion by option("-g", "--game-version", help = "Set a specific game version (latest by default)")
    private val supressUnavailable by option("-s", "--supress-unavailable", help = "Whether to supress mods which are not available for the given Minecraft version").flag()
    private val allResults by option("-a", "--all", help = "Whether to show all results without any limit").flag()
    private val limit by option("-l", "--limit", help = "The amount of results (defaults to 15)").int().default(15)

    override fun run() = runBlocking(Dispatchers.Default) {
        terminal.println("Searching with the given term '$searchTerm'")
        terminal.println()

        val versionRequest = async {
            when {
                gameVersion != null -> gameVersion
                else -> CurseProxy.getMinecraftVersions().first().versionString
            }
        }
        CurseProxy.search(
            searchTerm,
            if (supressUnavailable) versionRequest.await() else null,
            if (!allResults) limit else null
        )
            .apply {
                if (isEmpty())
                    terminal.warning("Could not find anything for the given term '$searchTerm'")
            }
            .forEach {
                terminal.printProject(it, versionRequest.await(), !supressUnavailable)
            }
    }
}
