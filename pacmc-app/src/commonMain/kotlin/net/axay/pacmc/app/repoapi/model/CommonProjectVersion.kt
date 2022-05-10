package net.axay.pacmc.app.repoapi.model

import kotlinx.datetime.Instant
import net.axay.pacmc.app.repoapi.repoApiContext
import net.axay.pacmc.common.data.MinecraftVersion
import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.common.data.Repository
import net.axay.pacmc.repoapi.CachePolicy
import net.axay.pacmc.repoapi.curseforge.model.FileRelationType
import net.axay.pacmc.repoapi.curseforge.model.FileReleaseType
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
    val type: Type,
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
        ) : Dependency() {

            suspend fun resolveModId(repository: Repository): ModId? {
                if (repository != Repository.MODRINTH)
                    error("Resolving version dependencies is only supported for Modrinth")
                return repoApiContext(CachePolicy.ONLY_FRESH) { c ->
                    c.getProjectVersion(ModSlug(repository, ""), id)?.modId
                }
            }
        }
    }

    enum class Type {
        RELEASE, BETA, ALPHA
    }

    companion object {
        fun fromModrinthProjectVersion(version: net.axay.pacmc.repoapi.modrinth.model.Version) = CommonProjectVersion(
            id = version.id,
            modId = ModId(Repository.MODRINTH, version.projectId),
            datePublished = version.datePublished,
            number = version.versionNumber,
            name = version.name,
            files = version.files.map { File(it.filename, it.url, it.primary) },
            gameVersions = version.gameVersions.mapNotNull { MinecraftVersion.fromString(it) },
            dependencies = version.dependencies.orEmpty().mapNotNull {
                val optional = it.dependencyType == BaseVersion.Dependencies.DependencyType.Optional
                when {
                    it.dependencyType == BaseVersion.Dependencies.DependencyType.Incompatible -> null
                    it.projectId != null -> Dependency.ProjectDependency(ModId(Repository.MODRINTH, it.projectId!!), optional)
                    it.versionId != null -> Dependency.VersionDependency(it.versionId!!, optional)
                    else -> null
                }
            },
            type = when (version.versionType) {
                BaseVersion.VersionType.Release -> Type.RELEASE
                BaseVersion.VersionType.Beta -> Type.BETA
                BaseVersion.VersionType.Alpha -> Type.ALPHA
            },
        )

        fun fromCurseforgeFile(file: net.axay.pacmc.repoapi.curseforge.model.File) = CommonProjectVersion(
            id = file.id.toString(),
            modId = ModId(Repository.CURSEFORGE, file.modId.toString()),
            datePublished = file.fileDate,
            number = file.fileName.removeSuffix(".jar"),
            name = file.displayName,
            files = listOf(File(file.fileName, file.downloadUrl, true)),
            gameVersions = file.sortableGameVersions.mapNotNull { MinecraftVersion.fromString(it.gameVersion) },
            dependencies = file.dependencies.mapNotNull {
                when (val relationType = FileRelationType.values().getOrNull(it.relationType - 1)) {
                    FileRelationType.REQUIRED_DEPENDENCY, FileRelationType.OPTIONAL_DEPENDENCY, FileRelationType.TOOL -> {
                        val optional = relationType != FileRelationType.REQUIRED_DEPENDENCY
                        Dependency.ProjectDependency(ModId(Repository.CURSEFORGE, it.modId.toString()), optional)
                    }
                    else -> null
                }
            },
            type = when (FileReleaseType.values().getOrNull(file.releaseType - 1)) {
                FileReleaseType.RELEASE -> Type.RELEASE
                FileReleaseType.BETA -> Type.BETA
                FileReleaseType.ALPHA -> Type.ALPHA
                null -> Type.RELEASE
            },
        )
    }
}
