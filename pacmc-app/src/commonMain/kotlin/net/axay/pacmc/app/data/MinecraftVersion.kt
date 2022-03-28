package net.axay.pacmc.app.data

data class MinecraftVersion(
    val bigMajor: Int,
    val major: Int,
    val minor: Int,
) : Comparable<MinecraftVersion> {

    override fun toString() = when (minor) {
        0 -> "${bigMajor}.${major}"
        else -> "${bigMajor}.${major}.${minor}"
    }

    override fun compareTo(other: MinecraftVersion): Int {
        val bigDiff = this.bigMajor.compareTo(other.bigMajor)
        if (bigDiff != 0) return bigDiff
        val diff = this.major.compareTo(other.major)
        if (diff != 0) return diff
        val minorDiff = this.minor.compareTo(other.minor)
        if (minorDiff != 0) return minorDiff
        return 0
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
