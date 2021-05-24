package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.axay.pacmc.Values
import net.axay.pacmc.ktorClient
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.terminal

object Search : CliktCommand() {
    private val searchterm by argument()
    private val gameversion by option("-g", "--game-version")

    private const val proxyApi = "https://addons-ecs.forgesvc.net/api/v2/addon/"

    override fun run() = runBlocking {
        withContext(Values.coroutineScope.coroutineContext) {
            ktorClient.get<List<CurseProxy.Project>>("${proxyApi}search") {
                parameter("gameId", 432)
                parameter("searchFilter", searchterm)
                if (gameversion != null) parameter("gameVersion", gameversion)
            }
        }.forEach { project ->
            val repo = yellow("curseforge/")
            val projectName = white(bold(project.name))
            val author = "by ${project.authors.first().name}"
            val version = project.latestVersion?.let { green(it) } ?: red("no file available")
            terminal.println("$repo$projectName $author $version")
            terminal.println("  ${gray(project.summary)}")
        }
    }
}
