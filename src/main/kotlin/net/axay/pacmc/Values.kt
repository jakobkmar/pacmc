package net.axay.pacmc

import dev.dirs.ProjectDirectories
import kotlinx.serialization.json.Json
import net.axay.pacmc.utils.OperatingSystem
import java.io.File

object Values {
    val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
    }

    val dataLocalDir by lazy {
        val dir = if (OperatingSystem.current != OperatingSystem.WINDOWS)
            File(ProjectDirectories.from("net", "axay", "pacmc").dataLocalDir)
        else File(System.getenv("LOCALAPPDATA"), "/axay/pacmc/data/")
        dir.mkdirs()
        dir
    }

    val dbFile by lazy { File(dataLocalDir, "/db1") }
}
