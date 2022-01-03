package net.axay.pacmc.app.data

data class MinecraftVersion(
    val bigMajor: Int,
    val major: Int,
    val minor: Int,
) {
    override fun toString() = when (minor) {
        0 -> "${bigMajor}.${major}"
        else -> "${bigMajor}.${major}.${minor}"
    }

    companion object {
        fun fromString(versionString: String): MinecraftVersion? {
            val splitString = versionString.trim().split('.')
                .map { it.toIntOrNull() ?: return null }

            if (splitString.size !in 2..3) return null

            return MinecraftVersion(splitString[0], splitString[1], splitString.getOrNull(2) ?: 0)
        }
    }
}
