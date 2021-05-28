package net.axay.pacmc

import kotlinx.serialization.json.Json

object Values {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }
}
