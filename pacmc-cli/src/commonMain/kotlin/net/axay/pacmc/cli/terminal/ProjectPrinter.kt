package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.axay.pacmc.app.database.model.DbInstalledProject
import net.axay.pacmc.app.repoapi.model.CommonProjectResult
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.app.repoapi.repoApiContext
import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.common.data.Repository

private val Repository.textColor
    get() = when (this) {
        Repository.MODRINTH -> TextColors.brightGreen
        Repository.CURSEFORGE -> TextColors.brightYellow
    }

private fun repoEntry(repository: Repository, entry: String): String {
    return repository.run { textColor(displayName.lowercase() + "/") } +
        TextColors.brightWhite(TextStyles.bold(TextStyles.underline(entry)))
}

val ModSlug.terminalString get() = repoEntry(repository, slug)

private val CommonProjectVersion.terminalString get() = repoEntry(
    modId.repository,
    (files.find { it.primary } ?: files.singleOrNull())?.name?.removeSuffix(".jar") ?: name
)

suspend fun ModId.terminalStringOrNull(): String? {
    return repoApiContext { it.getBasicProjectInfo(this@terminalStringOrNull) }
        ?.slug?.terminalString
}

suspend fun ModId.optimalTerminalString() = terminalStringOrNull() ?: repoEntry(repository, id)

suspend fun CommonProjectVersion.optimalTerminalString(): String {
    // TODO sometimes, only the request using slugs has been pre-cached
    val projectString = modId.terminalStringOrNull() ?: terminalString
    return projectString + " " + TextColors.brightCyan(number)
}

suspend fun DbInstalledProject.optimalTerminalString(): String = coroutineScope {
    val modId = readModId()

    val projectString = async { modId.optimalTerminalString() }

    val versionString = async {
        repoApiContext { it.getProjectVersion(modId, version) }
            ?.number ?: "version id: $version"
    }

    projectString.await() + " " + TextColors.brightCyan(versionString.await())
}

fun Terminal.printProject(project: CommonProjectResult) = println(buildString {
    append(project.slug.terminalString)
    project.latestVersion?.let { append(" ${TextColors.brightCyan(it.toString())}") }
    append(" ${TextStyles.italic("by")} ${project.author}")
    appendLine()
    append("  ${TextColors.gray(project.description)}")
})
