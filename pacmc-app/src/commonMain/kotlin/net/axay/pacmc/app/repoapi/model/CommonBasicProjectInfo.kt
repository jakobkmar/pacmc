package net.axay.pacmc.app.repoapi.model

import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.ModSlug
import net.axay.pacmc.app.data.Repository

data class CommonBasicProjectInfo(
    val id: ModId,
    val slug: ModSlug,
) {
    companion object {
        fun fromModrinthProject(project: net.axay.pacmc.repoapi.modrinth.model.Project) = CommonBasicProjectInfo(
            id = ModId(Repository.MODRINTH, project.id),
            slug = ModSlug(Repository.MODRINTH, project.slug.toString()),
        )
    }
}
