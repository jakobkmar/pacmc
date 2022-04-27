package net.axay.pacmc.app.repoapi.model

import kotlinx.datetime.Instant
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.repoapi.modrinth.model.BaseVersion

data class CommonProjectVersion(
    val id: String,
    val modId: ModId,
    val datePublished: Instant,
    val number: String,
    val name: String,
    val files: List<File>,
    val gameVersions: List<MinecraftVersion>,
    val dependencies: List<Dependency>,
) {
    data class File(
        val name: String,
        val url: String,
        val primary: Boolean,
    )

    sealed class Dependency {
        abstract val optional: Boolean

        data class ProjectDependency(
            val id: ModId,
            override val optional: Boolean,
        ) : Dependency()

        data class VersionDependency(
            val id: String,
            override val optional: Boolean,
        ) : Dependency()
    }

    companion object {
        fun fromModrinthProjectVersion(version: net.axay.pacmc.repoapi.modrinth.model.Version) = CommonProjectVersion(
            id = version.id,
            modId = ModId(Repository.MODRINTH, version.projectId),
            datePublished = version.datePublished,
            number = version.versionNumber,
            name = version.name,
            files = version.files.map { File(it.filename, it.url, it.primary) },
            gameVersions = version.gameVersions!!.mapNotNull { MinecraftVersion.fromString(it) },
            dependencies = version.dependencies.orEmpty().mapNotNull {
                val optional = it.dependencyType == BaseVersion.Dependencies.DependencyType.Optional
                when {
                    it.dependencyType == BaseVersion.Dependencies.DependencyType.Incompatible -> null
                    it.projectId != null -> Dependency.ProjectDependency(ModId(Repository.MODRINTH, it.projectId!!), optional)
                    it.versionId != null -> Dependency.VersionDependency(it.versionId!!, optional)
                    else -> null
                }
            },
        )
    }
}
