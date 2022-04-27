package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import net.axay.pacmc.app.data.ModSlug
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.repoapi.model.CommonProjectResult
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion

private val Repository.textColor
    get() = when (this) {
        Repository.MODRINTH -> TextColors.brightGreen
        Repository.CURSEFORGE -> TextColors.yellow
    }

val ModSlug.terminalString get() = buildString {
    append(repository.run { textColor(displayName.lowercase() + "/") })
    append(TextColors.white(TextStyles.bold(TextStyles.underline(slug))))
}

val CommonProjectVersion.terminalString get() = buildString {
    append(modId.repository.run { textColor(displayName.lowercase() + "/") })
    val printName = (files.find { it.primary } ?: files.singleOrNull())?.name?.removeSuffix(".jar") ?: name
    append(TextColors.white(TextStyles.bold(TextStyles.underline(printName))))
}

fun Terminal.printProject(project: CommonProjectResult) = println(buildString {
    append(project.slug.terminalString)
    project.latestVersion?.let { append(" ${TextColors.brightCyan(it.toString())}") }
    append(" ${TextStyles.italic("by")} ${project.author}")
    appendLine()
    append("  ${TextColors.gray(project.description)}")
})
