package net.axay.pacmc.storage

import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import net.axay.pacmc.storage.data.Archive
import java.io.File

object Xodus {
    val archiveStore: PersistentEntityStoreImpl
        get() = PersistentEntityStores.newInstance(File(System.getProperty("user.home"), ".pacmc/archives"))

    fun getArchive(name: String): Archive? {
        return archiveStore.use { store ->
            store.computeInTransaction {
                val archive = it.find("Archive", "name", name).first
                if (archive != null)
                    Archive(
                        archive.getProperty("name").toString(),
                        archive.getProperty("path").toString(),
                        archive.getProperty("gameVersion").toString()
                    )
                else null
            }
        }
    }
}
