package net.axay.pacmc.utils

enum class OperatingSystem {
    /**
     * ;)
     */
    LINUX,
    /**
     * for epic gamers
     */
    WINDOWS,
    /**
     * literally W T F
     */
    MACOS;

    companion object {
        val current = System.getProperty("os.name").lowercase().let { osName ->
            when {
                osName.contains("linux") -> LINUX
                osName.contains("windows") -> WINDOWS
                osName.contains("mac") -> MACOS
                else -> null
            }
        }
    }
}
