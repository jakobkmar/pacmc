package net.axay.pacmc.data

enum class ReleaseType {
    RELEASE, BETA, ALPHA;

    companion object {
        fun fromInt(int: Int) = when(int) {
            1 -> RELEASE
            2 -> BETA
            3 -> ALPHA
            else -> null
        }
    }
}
