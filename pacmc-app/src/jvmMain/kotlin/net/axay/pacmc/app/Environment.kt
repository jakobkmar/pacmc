package net.axay.pacmc.app

import dev.dirs.ProjectDirectories
import okio.FileSystem
import okio.Path.Companion.toPath

actual object Environment {
    private val projectDirectories = ProjectDirectories.from("net", "axay", "pacmc")

    actual val fileSystem = FileSystem.SYSTEM
    actual val dataLocalDir = projectDirectories.dataLocalDir.toPath()
    actual val cacheDir = projectDirectories.cacheDir.toPath()
    actual val osName get() = System.getProperty("os.name") ?: error("Could not get property 'os.name'")
}
