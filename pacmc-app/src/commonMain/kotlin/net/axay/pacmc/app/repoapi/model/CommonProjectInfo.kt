package net.axay.pacmc.app.repoapi.model

import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.Repository

data class CommonProjectInfo(
    val id: ModId,
    val slug: String,
    val name: String,
    val author: String,
    val description: String,
    val iconUrl: String?,
    val latestVersion: MinecraftVersion?,
) {
    companion object {
        fun fromModrinthProjectResult(projectResult: net.axay.pacmc.repoapi.modrinth.model.ProjectResult) = CommonProjectInfo(
            id = ModId(Repository.MODRINTH, projectResult.projectId),
            slug = projectResult.slug.toString(), // TODO slugs actually shouldn't be but there are old mods
            name = projectResult.title!!,
            author = projectResult.author,
            description = projectResult.description!!,
            iconUrl = projectResult.iconUrl,
            latestVersion = projectResult.versions.mapNotNull { MinecraftVersion.fromString(it) }.maxOrNull(),
        )
    }
}
