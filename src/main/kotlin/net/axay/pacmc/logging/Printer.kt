package net.axay.pacmc.logging

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.terminal.Terminal
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.data.Repository
import net.axay.pacmc.requests.common.data.CommonModResult
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.terminal

fun Terminal.printProject(
    project: CommonModResult,
    minecraftVersion: MinecraftVersion?,
    showAll: Boolean = true,
) {
    val version =
        if (minecraftVersion != null && !project.gameVersions.any { it.matchesMajor(minecraftVersion) })
            if (showAll) red("not available for ${minecraftVersion.versionString}") else return
        else if (project.gameVersions.isEmpty())
            red(italic("no version info"))
        else ""

    val repo = if (project.repository == Repository.MODRINTH)
        brightGreen("${project.repository}/")
    else
        yellow("${project.repository}/")
    val projectName = white(bold(underline(project.name)))
    val id = brightBlue("[${project.id}]")
    val author = "by ${project.author}"

    this.println("$repo$projectName $id $author $version")
    this.println("  ${gray(project.description ?: "no description available")}")
}

fun Terminal.printArchive(archive: DbArchive) {
    println("${red(archive.name)} at ${gray(archive.path)}")
}

fun formatMod(repository: Repository, name: String) =
    "${repository.coloredName}${white(bold(underline(name)))}"
