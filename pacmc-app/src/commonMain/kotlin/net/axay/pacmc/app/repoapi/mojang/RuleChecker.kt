package net.axay.pacmc.app.repoapi.mojang

import co.touchlab.kermit.Logger
import net.axay.pacmc.app.utils.OperatingSystem
import net.axay.pacmc.repoapi.mojang.model.VersionPackage

fun VersionPackage.Rule.check(): Boolean? {
    val result = when (action) {
        "allow" -> true
        "disallow" -> false
        else -> return null
    }

    os?.name?.let {
        if (it != OperatingSystem.current?.mojangName)
            return null
    }

    features?.forEach { (feature, active) ->
        when (feature) {
            "is_demo_user" -> if (active) return null
            "has_custom_resolution" -> { /* TODO */ }
            else -> Logger.w("Unknown feature '$feature' in rule, skipping it")
        }
    }

    return result
}
