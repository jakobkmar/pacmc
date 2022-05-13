package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.app.repoapi.resolveId
import net.axay.pacmc.app.utils.pmap
import net.axay.pacmc.cli.terminal
import net.axay.pacmc.common.data.MinecraftVersion
import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.common.data.Repository

suspend fun Archive.Companion.terminalFromString(name: String): Archive? {
    val archive = Archive(name)
    return if (archive.exists()) archive else {
        terminal.warning("The given archive '${archive.name}' does not exist")
        if (archive.name == ".minecraft") {
            terminal.warning("Try running ${TextColors.brightWhite("pacmc archive init")} to automatically detect the '.minecraft' folder.")
        }
        null
    }
}

fun MinecraftVersion.Companion.terminalFromString(version: String): MinecraftVersion? {
    val gameVersion = fromString(version)
    if (gameVersion == null) {
        terminal.warning("The given game version '$version' is invalid")
        return null
    }
    return gameVersion
}

object CliParser {
    private sealed class SlugResolveResult {
        class Resolved(val id: ModId) : SlugResolveResult()
        class Ambiguous(val possibleSlugs: List<Pair<ModSlug, ModId>>) : SlugResolveResult()
        class Invalid(val slug: String, val notFound: Boolean = false) : SlugResolveResult()
    }

    suspend fun resolveSlugs(
        rawSlugs: List<String>,
        archive: Archive? = null,
    ): Set<ModId>? {
        val spinner = SpinnerAnimation()
        spinner.start()

        val slugResolveResults = rawSlugs.pmap resolve@{ rawSlug ->
            spinner.update("resolving slug $rawSlug")
            val split = rawSlug.split('/')
            when (split.size) {
                1 -> {
                    val ids = Repository.values().toList().pmap { repo ->
                        ModSlug(repo, rawSlug).let { it to (it.resolveId() ?: return@pmap null) }
                    }.filterNotNull()

                    val singleResult = ids.singleOrNull()
                    if (singleResult != null) {
                        SlugResolveResult.Resolved(singleResult.second)
                    } else if (ids.isEmpty()) {
                        SlugResolveResult.Invalid(rawSlug, notFound = true)
                    } else {
                        val modId = archive?.getInstalled()?.singleOrNull {
                            val installedModId = it.readModId()
                            ids.any { (_, id) -> id == installedModId }
                        }?.readModId()

                        if (modId != null) {
                            SlugResolveResult.Resolved(modId)
                        } else {
                            SlugResolveResult.Ambiguous(ids)
                        }
                    }
                }
                2 -> {
                    val (repoName, slug) = split
                    val repo = try {
                        Repository.valueOf(repoName.uppercase())
                    } catch (exc: IllegalArgumentException) {
                        null
                    }

                    if (repo != null) {
                        val id = ModSlug(repo, slug).resolveId()
                        if (id != null) {
                            return@resolve SlugResolveResult.Resolved(id)
                        } else {
                            SlugResolveResult.Invalid(rawSlug, notFound = true)
                        }
                    } else {
                        SlugResolveResult.Invalid(rawSlug)
                    }
                }
                else -> SlugResolveResult.Invalid(rawSlug)
            }
        }

        spinner.stop("resolved slugs")

        val invalidSlugs = slugResolveResults.filterIsInstance<SlugResolveResult.Invalid>()
        if (invalidSlugs.isNotEmpty()) {
            invalidSlugs.forEach {
                if (it.notFound) {
                    terminal.warning("The project ${TextColors.brightRed(it.slug)} could not be found")
                } else {
                    terminal.warning("The slug ${TextColors.brightRed(it.slug)} is invalid")
                }
            }
            return null
        }

        var alreadyAsked = false

        return slugResolveResults.mapTo(HashSet()) { result ->
            when (result) {
                is SlugResolveResult.Resolved -> result.id
                is SlugResolveResult.Ambiguous -> {
                    if (!alreadyAsked) {
                        terminal.println()
                        alreadyAsked = true
                    }
                    val id = terminal.choose("Which one did you mean?", result.possibleSlugs.map { it.second to it.first.terminalString })
                    if (id == null) {
                        terminal.println("Abort.")
                        return null
                    } else {
                        terminal.println()
                        id
                    }
                }
                is SlugResolveResult.Invalid -> error("Unhandled invalid slug ${result.slug}")
            }
        }
    }
}
