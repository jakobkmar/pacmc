package net.axay.pacmc.repoapi.mojang

import io.ktor.client.*
import kotlinx.serialization.json.Json
import net.axay.memoire.Cache
import net.axay.pacmc.repoapi.AbstractRepositoryApi
import net.axay.pacmc.repoapi.RequestContext
import net.axay.pacmc.repoapi.mojang.model.VersionManifest
import net.axay.pacmc.repoapi.mojang.model.VersionPackage

class LauncherMetaApi(
    override val client: HttpClient,
    override val clientJson: Json,
    override val cache: Cache<String, String, String>?,
    override val apiUrl: String = "https://launchermeta.mojang.com",
) : AbstractRepositoryApi() {

    suspend fun RequestContext.getVersionManifest() =
        repoRequest<VersionManifest>("/mc/game/version_manifest_v2.json")

    suspend fun RequestContext.getVersionPackage(version: String): VersionPackage? {
        val url = getVersionManifest()?.versions?.find { it.id == version }?.url ?: return null
        return repoRequest<VersionPackage>(url.removePrefix(apiUrl))
    }
}
