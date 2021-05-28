package net.axay.pacmc.data

@Suppress("MemberVisibilityCanBePrivate")
data class MinecraftVersion(
    val first: Int,
    val second: Int,
    val third: Int? = null,
) {
    val isMajor get() = third == null

    val majorVersion get() = MinecraftVersion(first, second)

    val versionString get() = if (isMajor) majorVersionString else "$first.$second${if (third!! > 0) ".$third" else ""}"
    val majorVersionString get() = "$first.$second"

    fun matchesMajor(other: MinecraftVersion) = this.first == other.first && this.second == other.second

    fun minorDistance(other: MinecraftVersion): Int? {
        return when {
            this.isMajor || other.isMajor || !matchesMajor(other) -> null
            this.third == other.third -> 0
            else -> other.third!! - this.third!!
        }
    }

    companion object {
        fun fromString(versionString: String, major: Boolean = false): MinecraftVersion? {
            val split = versionString.split('.').map { it.toIntOrNull() }
            return if (
                split.size in 2..3 &&
                split.all { it != null }
            ) MinecraftVersion(split[0]!!, split[1]!!, split.getOrNull(2) ?: if (major) null else 0) else null
        }
    }
}
