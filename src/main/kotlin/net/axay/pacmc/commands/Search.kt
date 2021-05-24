package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
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
    private const val proxyApi = "https://addons-ecs.forgesvc.net/api/v2/addon/"

    private val searchterm by argument()

    override fun run() = runBlocking {
        withContext(Values.coroutineScope.coroutineContext) {
            ktorClient.get<List<CurseProxy.Project>>("${proxyApi}search?gameId=432&sectionId=6&searchFilter=$searchterm")
        }.forEach {
            terminal.println("${yellow("curseforge/")}${white(bold(it.name))} by ${it.authors.first().name} ${green(it.latestVersion)}")
            terminal.println("  ${gray(it.summary)}")
        }
    }
}
