package net.axay.pacmc.app.repoapi.model

import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.common.data.Repository

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
