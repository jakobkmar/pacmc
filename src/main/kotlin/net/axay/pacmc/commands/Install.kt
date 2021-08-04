package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.ktorClient
import net.axay.pacmc.logging.printProject
import net.axay.pacmc.requests.common.RepositoryApi
import net.axay.pacmc.requests.common.data.CommonModInfo
import net.axay.pacmc.requests.common.data.CommonModResult
import net.axay.pacmc.requests.common.data.CommonModVersion
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.data.PacmcFile
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.execAsyncBatch
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import org.kodein.db.find
import org.kodein.db.useModels
import java.io.File
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.any
import kotlin.collections.emptyList
import kotlin.collections.filterNot
import kotlin.collections.first
import kotlin.collections.flatten
import kotlin.collections.fold
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.minOrNull
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.setOf
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object Install : CliktCommand(
    "Installs a minecraft mod"
) {
    private val archiveName by option("-a", "--archive", help = "The archive where the mod should be installed").default(".minecraft")

    private val mod by argument(help = "The name or the ID of the mod you want to install")

    override fun run() = runBlocking(Dispatchers.Default) {
        terminal.println("Searching for installable mods matching '$mod'")

        val archive = db.getArchiveOrWarn(archiveName) ?: return@runBlocking

        var modId = mod
        var files: List<CommonModVersion>? = null

        suspend fun updateFiles() {
            files = RepositoryApi.getModVersions(modId)
        }

        // start search already, because the user likely entered a mod name or slug and not an id
        val searchResultsDeferred = async {
            RepositoryApi.search(mod, 9)
        }

        updateFiles()

        fun notFoundMessage() = terminal.danger("Could not find anything for the given mod '$mod'")

        // search for the given mod if it was not a valid ID
        if (files == null || files!!.isEmpty()) {
            val searchResults = searchResultsDeferred.await()
            modId = when {
                // ask the user which mod he wants to install
                searchResults.size > 1 -> {
                    val options = HashMap<Int, CommonModResult>()

                    terminal.println()
                    searchResults.forEachIndexed { index, project ->
                        options[index + 1] = project

                        terminal.print(TextColors.rgb(50, 255, 236)("${index + 1}) "))
                        terminal.printProject(project, archive.minecraftVersion, true)
                    }
                    terminal.println()

                    var choice: Int? = null
                    while (choice == null) {
                        terminal.print("Which mod do you want to install? (${options.keys.joinToString()}) ")
                        val readLine = (readLine() ?: return@runBlocking).toIntOrNull()
                        choice = when (readLine) {
                            null -> null
                            in 1..searchResults.size -> readLine
                            else -> null
                        }
                    }

                    terminal.println()

                    options[choice]!!.id
                }
                // just take the one matching mod
                searchResults.size == 1 -> searchResults.first().id
                else -> {
                    notFoundMessage()
                    return@runBlocking
                }
            }

            updateFiles()
        } else {
            searchResultsDeferred.cancel()
        }

        if (files == null) {
            notFoundMessage()
            return@runBlocking
        }

        val modInfo = async { RepositoryApi.getModInfo(modId)!! }

        val file = files?.findBestFile(archive.minecraftVersion)?.first ?: kotlin.run {
            terminal.danger("Could not find a release of '$mod' for the minecraft version '${archive.minecraftVersion.versionString}'")
            return@runBlocking
        }

        val dependenciesDeferred = async { findDependencies(file, archive.minecraftVersion) }

        terminal.println("Installing the mod at ${gray(archive.path)}")
        terminal.println()

        downloadFile(modId, file, modInfo, true, archive)

        val dependencies = dependenciesDeferred.await()
        if (dependencies.isNotEmpty()) {
            terminal.println()
            terminal.println("Resolving dependencies...")
            terminal.println()

            dependencies.forEach {
                downloadFile(it.addonId, it.file, it.info, false, archive)
            }
        }

        terminal.println()
        terminal.println(brightGreen("Successfully installed the given mod."))
    }

    fun List<CommonModVersion>.findBestFile(minecraftVersion: MinecraftVersion) = this
        .filterNot { (it.loaders.contains("forge") || it.loaders.contains("rift")) && !it.loaders.contains("fabric") }
        .fold<CommonModVersion, Pair<CommonModVersion, Int>?>(null) { acc, modVersion ->
            val distance = modVersion.gameVersions
                .mapNotNull { it.minorDistance(minecraftVersion) }
                .minOrNull()

            when {
                // this file does not have the same major version
                distance == null -> acc
                // the previous file is not fitting and this one is
                acc == null -> modVersion to distance
                // prefer the file closer to the desired version
                acc.second.absoluteValue > distance.absoluteValue -> modVersion to distance
                // both files are similarly close to the desired version, do additional checks
                acc.second.absoluteValue == distance.absoluteValue -> when {
                    // prefer the file for the newer version
                    acc.second > 0 && distance < 0 -> acc
                    acc.second < 0 && distance > 0 -> modVersion to distance
                    // prefer the newer file
                    else -> if (acc.first.datePublished > modVersion.datePublished)
                        acc else modVersion to distance
                }
                else -> acc
            }
        }

    class ResolvedDependency(
        val file: CommonModVersion,
        val addonId: String,
        val info: Deferred<CommonModInfo>,
    )

    suspend fun findDependencies(file: CommonModVersion, minecraftVersion: MinecraftVersion): List<ResolvedDependency> =
        coroutineScope {
            return@coroutineScope file.dependencies.map { dependency ->
                async {
                    val dependencyFile = (
                        if (dependency.versionId == null)
                            RepositoryApi.getModVersions(dependency.modId)?.findBestFile(minecraftVersion)?.first
                        else
                            RepositoryApi.getModVersion(dependency.versionId)
                    ) ?: return@async emptyList()
                    // save the addonId, because it is not part of the response
                    setOf(ResolvedDependency(
                        dependencyFile,
                        dependency.modId,
                        async { RepositoryApi.getModInfo(dependency.modId)!! }
                    )) + findDependencies(dependencyFile, minecraftVersion).filterNot { it.addonId == dependency.modId }
                }
            }.awaitAll().flatten()
        }

    suspend fun downloadFile(
        modId: String,
        modVersion: CommonModVersion,
        modInfo: Deferred<CommonModInfo>?,
        persistent: Boolean,
        archive: DbArchive,
    ) = coroutineScope {
        val versionId = modVersion.id

        // download the mod file to the given archive (and display progress)
        terminal.println("Downloading " + brightCyan(modVersion.name))

        db.execAsyncBatch {
            val existingMod = db.find<DbMod>()
                .byIndex("archiveRepoIdIndex", modVersion.repository, modId, archive.name)
                .useModels { it.firstOrNull() }
            if (existingMod != null) {
                val newPersistence = if (persistent) true else existingMod.persistent
                put(existingMod.copy(persistent = newPersistence, version = versionId))
            } else {
                val resolvedModInfo = runBlocking { modInfo?.await() }
                    ?: error("Resolved mod info is not provided upon first mod download")

                put(DbMod(
                    modVersion.repository, modId,
                    versionId,
                    resolvedModInfo.name, resolvedModInfo.slug, resolvedModInfo.description,
                    persistent,
                    archive.name
                ))
            }
        }

        if (archive.pacmcFiles.any { it.modId == modId && it.versionId == versionId }) {
            terminal.println("  already installed ${green("✔")}")
            return@coroutineScope
        }

        archive.files.forEach {
            if (it.second.modId == modId)
                it.first.delete()
        }

        modVersion.files.forEachIndexed { modFileIndex, modFile ->
            val filename = PacmcFile(modVersion.repository, modId, versionId, modFileIndex).filename
            val localFile = File(archive.path, filename)

            val downloadContent = ktorClient.get<HttpResponse>(modFile.url) {
                onDownload { bytesSentTotal, contentLength ->
                    val progress = bytesSentTotal.toDouble() / contentLength.toDouble()
                    val dashCount = (progress * 30).roundToInt()
                    val percentage = (progress * 100).roundToInt()
                    launch(Dispatchers.IO) {
                        val string = buildString {
                            append('[')
                            repeat(dashCount) {
                                append(green("─"))
                            }
                            append(green(">"))
                            repeat(30 - dashCount) {
                                append(' ')
                            }
                            append("] ${percentage}%")
                        }
                        terminal.print("\r  $string")
                    }.join()
                }
            }.content
            terminal.println()

            downloadContent.copyAndClose(localFile.writeChannel())
        }
    }
}
