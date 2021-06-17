package net.axay.pacmc

import dev.dirs.ProjectDirectories
import kotlinx.serialization.json.Json

object Values {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    val projectDirectories: ProjectDirectories =
        ProjectDirectories.from("net", "axay", "pacmc")
}
