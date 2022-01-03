package net.axay.pacmc.app.features

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.realm.objects
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModFile
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.database.realm
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.app.repoapi.RepositoryApi
import okio.Path.Companion.toPath

class Archive(val name: String) {
    private lateinit var dbArchive: DbArchive

    private fun updateFromDb(): DbArchive? {
        dbArchive = realm.objects<DbArchive>().query("name == $0 LIMIT(1)", name).firstOrNull() ?: return null
        return dbArchive
    }

    suspend fun create(minecraftVersion: MinecraftVersion, loader: ModLoader) {
        val archive = DbArchive().apply {
            this.name = this@Archive.name
            this.minecraftVersion = minecraftVersion.toString()
            this.loader = loader
        }

        realm.write {
            copyToRealm(archive)
        }
    }

    suspend fun install(
        modId: ModId,
        downloadProgress: (Double) -> Unit,
    ) = coroutineScope scope@{
        updateFromDb() ?: return@scope

        if (dbArchive.installed.any { it.id == modId.id }) return@scope

        val projectVersionsDeferred = async {
            RepositoryApi.getProjectVersions(
                modId,
                listOf(dbArchive.loader),
                listOf(dbArchive.readMinecraftVersion())
            )
        }
        val projectInfoDeferred = async {
            RepositoryApi.getProject(modId)
        }

        val version = projectVersionsDeferred.await()?.maxByOrNull { it.datePublished } ?: return@scope

        val downloadFile = version.files.firstOrNull { it.primary } ?: return@scope

        val fileBytes = ktorClient.get<HttpResponse>(downloadFile.url) {
            onDownload { bytesSentTotal, contentLength ->
                downloadProgress(bytesSentTotal.toDouble() / contentLength.toDouble())
            }
        }.receive<ByteArray>()

        val fileName = ModFile(modId.repository.shortForm, projectInfoDeferred.await()?.slug, modId.id).fileName

        Environment.fileSystem.write(dbArchive.path.toPath().resolve(fileName)) {
            write(fileBytes)
        }

        // TODO: add mod to list of installed
        dbArchive.installed
        realm.write {
            copyToRealm(dbArchive)
        }
    }
}
