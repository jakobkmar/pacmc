package net.axay.pacmc.app

import dev.dirs.ProjectDirectories
import net.axay.pacmc.app.utils.OperatingSystem
import okio.FileSystem
import okio.Path.Companion.toPath

actual object Environment {
    private val projectDirectories = ProjectDirectories.from("org", null, "pacmc")

    private fun getProperty(name: String) = System.getProperty(name) ?: error("Could not get property '${name}'")

    actual val fileSystem = FileSystem.SYSTEM

    actual val dataLocalDir = if (OperatingSystem.notWindows)
        projectDirectories.dataLocalDir.toPath()
    else
        getEnv("LOCALAPPDATA")!!.toPath().resolve("pacmc/data")

    actual val cacheDir = if (OperatingSystem.notWindows)
        projectDirectories.cacheDir.toPath()
    else
        getEnv("LOCALAPPDATA")!!.toPath().resolve("pacmc/cache")

    actual val configDir = if (OperatingSystem.notWindows)
        projectDirectories.configDir.toPath()
    else
        getEnv("APPDATA")!!.toPath().resolve("pacmc/config")

    actual val osName get() = getProperty("os.name")
    actual val userHome get() = getProperty("user.home").toPath()

    actual fun getEnv(name: String) = try {
        System.getenv(name)
    } catch (exc: Exception) {
        null
    }
}
