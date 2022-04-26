package net.axay.pacmc.app.repoapi.model

import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.ModSlug
import net.axay.pacmc.app.data.Repository

data class CommonProject(
    val id: ModId,
    val slug: ModSlug,
    val name: String,
    val description: String,
) {
    companion object {
        fun fromModrinthProject(project: net.axay.pacmc.repoapi.modrinth.model.Project) = CommonProject(
            id = ModId(Repository.MODRINTH, project.id),
            slug = ModSlug(Repository.MODRINTH, project.slug),
            name = project.title,
            description = project.description,
        )
    }
}
