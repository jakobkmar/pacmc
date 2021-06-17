package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.ktorClient
import net.axay.pacmc.logging.printProject
import net.axay.pacmc.requests.CurseProxy
import net.axay.pacmc.requests.data.CurseProxyFile
import net.axay.pacmc.requests.data.CurseProxyProject
import net.axay.pacmc.storage.Xodus
import net.axay.pacmc.terminal
import java.io.File
import kotlin.collections.set
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object Install : CliktCommand(
    "Installs a minecraft mod"
) {
    private val local by option("-l", "--local").flag()
    private val archive by option("-a", "--archive").default(".minecraft")

    private val mod by argument()

    override fun run() = runBlocking(Dispatchers.Default) {
        val archive = Xodus.getArchive(archive) ?: return@runBlocking

        var modId: Int? = mod.toIntOrNull()
        var files: List<CurseProxyFile>? = null

        suspend fun updateFiles() {
            if (modId != null)
                files = try {
                    CurseProxy.getModFiles(modId!!)
                } catch (exc: ClientRequestException) {
                    null
                }
        }

        updateFiles()

        // search for the given mod if it was not a valid ID
        if (files == null || files!!.isEmpty()) {
            val searchResults = CurseProxy.search(mod, null, 9)
            modId = when {
                // ask the user which mod he wants to install
                searchResults.size > 1 -> {
                    val options = HashMap<Int, CurseProxyProject>()

                    searchResults.forEachIndexed { index, project ->
                        options[index + 1] = project

                        terminal.print(TextColors.rgb(50, 255, 236)("${index + 1}) "))
                        terminal.printProject(project, archive.gameVersion, true)
                    }
                    echo()

                    print("Which mod do you want to install? (${options.keys.joinToString()}) ")
                    var choice: Int? = null
                    while (choice == null) {
                        val readLine = (readLine() ?: return@runBlocking).toIntOrNull()
                        choice = when (readLine) {
                            null -> null
                            in 1..searchResults.size -> readLine
                            else -> null
                        }
                    }

                    options[choice]?.id
                }
                // just take the one matching mod
                searchResults.size == 1 -> searchResults.first().id
                else -> null
            }

            updateFiles()
        }

        val fileResult = files
            ?.filterNot { it.gameVersion.contains("Forge") && !it.gameVersion.contains("Fabric") }
            ?.fold<CurseProxyFile, Pair<CurseProxyFile, Int>?>(null) { acc, curseProxyFile ->
                val distance = curseProxyFile.minecraftVersions
                    .mapNotNull { it.minorDistance(archive.minecraftVersion) }
                    .minOrNull()

                when {
                    // this file does not have the same major version
                    distance == null -> acc
                    // the previous file is not fitting and this one is
                    acc == null -> curseProxyFile to distance
                    // prefer the file closer to the desired version
                    acc.second.absoluteValue > distance.absoluteValue -> curseProxyFile to distance
                    // both files are similarly close to the desired version, do additional checks
                    acc.second.absoluteValue == distance.absoluteValue -> when {
                        // prefer the file for the newer version
                        acc.second > 0 && distance < 0 -> acc
                        acc.second < 0 && distance > 0 -> curseProxyFile to distance
                        // prefer the newer file
                        else -> if (acc.first.releaseDate.isAfter(curseProxyFile.releaseDate))
                            acc else curseProxyFile to distance
                    }
                    else -> acc
                }
            }

        if (fileResult == null) {
            echo("Could not find anything for the given mod \"$mod\"")
            return@runBlocking
        }
        val file = fileResult.first

        echo("Found: $file")
        echo("Installing the mod at ${archive.path}")

        // download the mod file to the given archive (and display progress)
        print("bei 0%")
        ktorClient.get<HttpResponse>(file.downloadUrl) {
            onDownload { bytesSentTotal, contentLength ->
                print("\rbei ${((bytesSentTotal.toDouble() / contentLength.toDouble()) * 100).roundToInt()}%")
            }
        }.content.copyAndClose(File(archive.path, "pacmc_curseforge_${modId}.jar").writeChannel())
        println()

        echo("Finishing")
    }
}
