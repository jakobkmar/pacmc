package net.axay.pacmc

import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import kotlinx.serialization.json.Json
import java.io.File

object Values {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    val archiveStore: PersistentEntityStoreImpl
        get() = PersistentEntityStores.newInstance(File(System.getProperty("user.home"), ".pacmc/archives"))
}
