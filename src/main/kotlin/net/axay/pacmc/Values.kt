package net.axay.pacmc

import dev.dirs.ProjectDirectories
import kotlinx.serialization.json.Json

object Values {
    val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
    }

    val projectDirectories: ProjectDirectories by lazy {
        ProjectDirectories.from("net", "axay", "pacmc")
    }
}
