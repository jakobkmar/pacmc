package net.axay.pacmc.repoapi.mojang

import io.ktor.client.*
import net.axay.pacmc.repoapi.AbstractRepositoryApi
import net.axay.pacmc.repoapi.mojang.model.VersionManifest

class LauncherMetaApi(
    override val apiUrl: String = "https://launchermeta.mojang.com/mc",
    override val client: HttpClient,
) : AbstractRepositoryApi() {
    suspend fun getVersionManifest() =
        repoRequest<VersionManifest>("/game/version_manifest.json")
}
