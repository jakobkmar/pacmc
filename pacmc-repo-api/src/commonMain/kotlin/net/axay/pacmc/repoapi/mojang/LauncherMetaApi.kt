package net.axay.pacmc.repoapi.mojang

import io.ktor.client.*
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache
import net.axay.pacmc.repoapi.AbstractRepositoryApi
import net.axay.pacmc.repoapi.RequestContext
import net.axay.pacmc.repoapi.mojang.model.VersionManifest

class LauncherMetaApi(
    override val client: HttpClient,
    override val clientJson: Json,
    override val cache: Cache<String, String, String>?,
    override val apiUrl: String = "https://launchermeta.mojang.com/mc",
) : AbstractRepositoryApi() {

    suspend fun RequestContext.getVersionManifest() =
        repoRequest<VersionManifest>("/game/version_manifest_v2.json")
}
