package net.axay.pacmc.app.utils

import net.axay.pacmc.app.Environment

enum class OperatingSystem(
    val prefix: String,
    val displayName: String,
) {
    LINUX("linux", "Linux"),
    MACOS("mac", "macOS"),
    WINDOWS("windows", "Microsoft Windows");

    companion object {
        val current = Environment.osName.lowercase().let { osName ->
            values().singleOrNull { osName.startsWith(it.prefix) }
        }

        val notWindows get() = current != WINDOWS
    }
}
