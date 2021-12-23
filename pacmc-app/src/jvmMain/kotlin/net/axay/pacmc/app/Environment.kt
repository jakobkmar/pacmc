package net.axay.pacmc.app

import dev.dirs.ProjectDirectories
import okio.FileSystem
import okio.Path.Companion.toPath

actual object Environment {
    actual val fileSystem = FileSystem.SYSTEM
    actual val dataLocalDir = ProjectDirectories.from("net", "axay", "pacmc").dataLocalDir.toPath()
    actual val osName get() = System.getProperty("os.name") ?: error("Could not get property 'os.name'")
}
