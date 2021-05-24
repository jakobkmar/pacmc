package net.axay.pacmc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

object Values {
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }
}
