package net.axay.pacmc.app.utils

import com.github.ajalt.colormath.model.HSL

object ColorUtils {
    fun randomLightColor() = HSL((0..360).random(), 1f, 0.75f).toSRGB()
}
