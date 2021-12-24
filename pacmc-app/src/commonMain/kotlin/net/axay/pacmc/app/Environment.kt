package net.axay.pacmc.app

import okio.FileSystem
import okio.Path

expect object Environment {
    val fileSystem: FileSystem
    val dataLocalDir: Path
    val cacheDir: Path
    val osName: String
}
