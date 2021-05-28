package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.data.ReleaseType
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.terminal

object Search : CliktCommand(
    "Search for mods"
) {
    private val searchTerm by argument()
    private val gameVersion by option("-g", "--game-version", help = "Set a specific game version (latest by default)")
    private val allVersions by option("-i", "--all-versions", help = "Whether to show mods for all Minecraft versions").flag()
    private val allResults by option("-a", "--all", help = "Whether to show all result without any limit").flag()
    private val limit by option("-l", "--limit", help = "The amount of results (defaults to 15)").int().default(15)

    private val versionChars = arrayOf('.', '-', '+', '_', ' ')

    override fun run() = runBlocking(Dispatchers.IO) {
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
        ).forEach { project ->
            val mcVersion = versionRequest.await()

            val repo = yellow("curseforge/")
            val id = brightBlue("[${project.id}]")
            val projectName = white(bold(underline(project.name)))
            val author = "by ${project.authors.firstOrNull()?.name ?: italic("unknown author")}"
            val version = project.getLatestVersion(mcVersion)
                ?.let { latest ->
                    val versionString = latest.first.lowercase().trim { it in versionChars || it.isLetter() }
                    if (versionString.isNotEmpty()) {
                        green(bold(versionString)).let {
                            when (latest.second) {
                                ReleaseType.BETA -> it + cyan(" (beta)")
                                ReleaseType.ALPHA -> it + magenta(" (alpha)")
                                else -> it
                            }
                        }
                    } else red(italic("no version info"))
                }
                ?: if (mcVersion != null)
                    if (allVersions) red("not available for $mcVersion") else return@forEach
                else
                    red(italic("no file info"))

            terminal.println("$repo$projectName $id $author $version")
            terminal.println("  ${gray(project.summary)}")
        }
    }
}
