package net.axay.pacmc.logging

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import net.axay.pacmc.data.ReleaseType
import net.axay.pacmc.requests.data.CurseProxyProject

private val versionChars = arrayOf('.', '-', '+', '_', ' ')

fun Terminal.printProject(
    project: CurseProxyProject,
    minecraftVersion: String?,
    showAll: Boolean = true,
) {
    val repo = TextColors.yellow("curseforge/")
    val id = TextColors.brightBlue("[${project.id}]")
    val projectName = TextColors.white(TextStyles.bold(TextStyles.underline(project.name)))
    val author = "by ${project.authors.firstOrNull()?.name ?: TextStyles.italic("unknown author")}"
    val version = project.getLatestVersion(minecraftVersion)
        ?.let { latest ->
            val versionString = latest.first.lowercase().trim { it in versionChars || it.isLetter() }
            if (versionString.isNotEmpty()) {
                TextColors.green(TextStyles.bold(versionString)).let {
                    when (latest.second) {
                        ReleaseType.BETA -> it + TextColors.cyan(" (beta)")
                        ReleaseType.ALPHA -> it + TextColors.magenta(" (alpha)")
                        else -> it
                    }
                }
            } else TextColors.red(TextStyles.italic("no version info"))
        }
        ?: if (minecraftVersion != null)
            if (showAll) TextColors.red("not available for $minecraftVersion") else return
        else
            TextColors.red(TextStyles.italic("no file info"))

    this.println("$repo$projectName $id $author $version")
    this.println("  ${TextColors.gray(project.summary)}")
}
