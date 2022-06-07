package net.axay.pacmc.app.repoapi.mojang

import co.touchlab.kermit.Logger
import net.axay.pacmc.app.utils.OperatingSystem
import net.axay.pacmc.repoapi.mojang.model.VersionPackage

fun List<VersionPackage.Rule>?.check(): Boolean {
    if (this == null) return true
    if (this.isEmpty()) return true

    var allowed = false

    for (rule in this) {
        val result = when (rule.action) {
            "allow" -> true
            "disallow" -> false
            else -> {
                Logger.w("Unknown rule action '${rule.action}' in rule, skipping it")
                continue
            }
        }

        var doesApply = true

        kotlin.run {
            rule.os?.name?.let {
                if (it != OperatingSystem.current?.mojangName) {
                    doesApply = false
                    return@run
                }
            }

            rule.features?.forEach { (feature, active) ->
                when (feature) {
                    "is_demo_user" -> if (active) {
                        doesApply = false
                        return@run
                    }
                    "has_custom_resolution" -> { /* TODO */ }
                    else -> Logger.w("Unknown feature '${feature}' in rule, skipping it")
                }
            }
        }

        if (doesApply) {
            if (result) allowed = true else return false
        }
    }

    return allowed
}
