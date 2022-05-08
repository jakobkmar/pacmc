package net.axay.pacmc.app

import dev.dirs.ProjectDirectories
import okio.FileSystem
import okio.Path.Companion.toPath

actual object Environment {
    private val projectDirectories = ProjectDirectories.from("org", null, "pacmc")

    private fun getProperty(name: String) = System.getProperty(name) ?: error("Could not get property '${name}'")

    actual val fileSystem = FileSystem.SYSTEM
    actual val dataLocalDir = projectDirectories.dataLocalDir.toPath()
    actual val cacheDir = projectDirectories.cacheDir.toPath()
    actual val osName get() = getProperty("os.name")
    actual val userHome get() = getProperty("user.home").toPath()
}
