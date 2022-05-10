package net.axay.pacmc.app.repoapi.model

import net.axay.pacmc.common.data.MinecraftVersion
import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.common.data.Repository

data class CommonProjectResult(
    val id: ModId,
    val slug: ModSlug,
    val name: String,
    val author: String,
    val description: String,
    val iconUrl: String?,
    val latestVersion: MinecraftVersion?,
) {
    companion object {
        fun fromModrinthProjectResult(projectResult: net.axay.pacmc.repoapi.modrinth.model.ProjectResult) = CommonProjectResult(
            id = ModId(Repository.MODRINTH, projectResult.projectId),
            slug = ModSlug(Repository.MODRINTH, projectResult.slug.toString()), // TODO slugs actually shouldn't be but there are old mods
            name = projectResult.title!!,
            author = projectResult.author,
            description = projectResult.description!!,
            iconUrl = projectResult.iconUrl,
            latestVersion = projectResult.versions.mapNotNull { MinecraftVersion.fromString(it) }.maxOrNull(),
        )

        fun fromCurseforgeMod(mod: net.axay.pacmc.repoapi.curseforge.model.Mod) = CommonProjectResult(
            id = ModId(Repository.CURSEFORGE, mod.id.toString()),
            slug = ModSlug(Repository.CURSEFORGE, mod.slug),
            name = mod.name,
            author = mod.authors.first().name,
            description = mod.summary,
            iconUrl = mod.logo.thumbnailUrl,
            latestVersion = mod.latestFilesIndexes.mapNotNull { MinecraftVersion.fromString(it.gameVersion) }.maxOrNull(),
        )
    }
}
