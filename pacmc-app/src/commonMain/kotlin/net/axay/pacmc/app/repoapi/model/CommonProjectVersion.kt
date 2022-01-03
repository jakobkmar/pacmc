package net.axay.pacmc.app.repoapi.model

import kotlinx.datetime.Instant

data class CommonProjectVersion(
    val id: String,
    val datePublished: Instant,
    val files: List<File>,
) {
    data class File(
        val url: String,
        val primary: Boolean,
    )

    companion object {
        fun fromModrinthProjectVersion(version: net.axay.pacmc.repoapi.modrinth.model.Version) = CommonProjectVersion(
            id = version.id,
            datePublished = Instant.parse(version.datePublished),
            files = version.files?.map { File(it.url, it.primary) }.orEmpty(),
        )
    }
}
