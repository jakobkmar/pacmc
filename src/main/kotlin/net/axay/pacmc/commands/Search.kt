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
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.logging.printProject
import net.axay.pacmc.requests.common.RepositoryApi
import net.axay.pacmc.requests.curse.CurseProxy
import net.axay.pacmc.terminal

object Search : CliktCommand(
    "Searches for mods"
) {
    private val searchTerm by argument()
    private val gameVersion by option("-g", "--game-version", help = "Set a specific game version (latest by default)")
    private val suppressUnavailable by option("-s", "--suppress-unavailable", help = "Whether to suppress mods which are not available for the given Minecraft version").flag()
    private val limit by option("-l", "--limit", help = "The amount of results (defaults to 15)").int().default(5)

    override fun run() = runBlocking(Dispatchers.Default) {
        terminal.println("Searching with the given term '$searchTerm'")

        val versionRequest = async {
            val versionString = when {
                gameVersion != null -> gameVersion!!
                else -> CurseProxy.getMinecraftVersions().first().versionString
            }
            MinecraftVersion.fromString(versionString)
        }

        RepositoryApi.search(searchTerm, limit, limit, showWaitingMessage = true)
            .apply {
                terminal.println()
                if (isEmpty())
                    terminal.warning("Could not find anything for the given term '$searchTerm'")
            }
            .forEach { modResult ->
                terminal.printProject(
                    modResult,
                    versionRequest.await(),
                    !suppressUnavailable
                )
            }
    }
}
