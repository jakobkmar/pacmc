package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.repoapi.model.CommonProjectResult

private val Repository.textColor
    get() = when (this) {
        Repository.MODRINTH -> TextColors.brightGreen
        Repository.CURSEFORGE -> TextColors.yellow
    }

fun Terminal.printProject(project: CommonProjectResult) = println(buildString {
    append(project.id.repository.run { textColor(displayName.lowercase() + "/") })
    append(TextColors.white(TextStyles.bold(TextStyles.underline(project.slug.slug))))
    project.latestVersion?.let { append(" ${TextColors.brightCyan(it.toString())}") }
    append(" ${TextStyles.italic("by")} ${project.author}")
    appendLine()
    append("  ${TextColors.gray(project.description)}")
})
