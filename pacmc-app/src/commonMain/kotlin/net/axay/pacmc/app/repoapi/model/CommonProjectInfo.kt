package net.axay.pacmc.app.repoapi.model

data class CommonProjectInfo(
    val id: String,
    val slug: String?,
    val name: String,
    val author: String,
    val description: String,
    val iconUrl: String?,
) {
    companion object {
        fun fromModrinthProjectResult(projectResult: net.axay.pacmc.repoapi.modrinth.model.ProjectResult) = CommonProjectInfo(
            id = projectResult.projectId,
            slug = projectResult.slug,
            name = projectResult.title,
            author = projectResult.author,
            description = projectResult.description,
            iconUrl = projectResult.iconUrl,
        )
    }
}
