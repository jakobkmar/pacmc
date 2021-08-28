package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.data.Repository
import net.axay.pacmc.logging.formatMod
import net.axay.pacmc.requests.modrinth.ModrinthApi
import net.axay.pacmc.terminal

object Info : CliktCommand(
    "Displays the formatted project description"
) {
    private val query by argument()

    override fun run(): Unit = runBlocking(Dispatchers.Default) {
        val result = ModrinthApi.search(query, 1).hits.firstOrNull()?.convertToCommon()

        if (result != null) {
            val body = ModrinthApi.getModDescriptionBody(result.id)?.body

            if (body != null) {
                kotlin.runCatching {
                    terminal.println("${TextStyles.italic("Project:")} " + formatMod(Repository.MODRINTH, result.name))
                    terminal.println("${TextStyles.italic("Summary:")} ${result.description}")
                    terminal.println()
                    terminal.println(Markdown(body))
                }.onFailure {
                    terminal.danger("Failed to render the mod description")
                }
            } else {
                terminal.danger("The given mod exists, but no description body could be found for it!")
            }
        } else {
            terminal.danger("Couldn't find anything on Modrinth for the given mod query")
            terminal.info("Keep in mind that Curseforge is not supported by this command")
        }
    }
}
