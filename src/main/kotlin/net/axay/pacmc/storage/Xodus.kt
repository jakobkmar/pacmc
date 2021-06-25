package net.axay.pacmc.storage

import jetbrains.exodus.database.TransientStoreSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.dnq.XdModel
import kotlinx.dnq.query.eq
import kotlinx.dnq.query.firstOrNull
import kotlinx.dnq.query.query
import kotlinx.dnq.store.container.StaticStoreContainer
import kotlinx.dnq.util.initMetaData
import net.axay.pacmc.Values
import net.axay.pacmc.storage.data.XdArchive
import net.axay.pacmc.storage.data.XdMod
import net.axay.pacmc.terminal
import java.io.File

object Xodus {
    init {
        XdModel.registerNodes(XdArchive, XdMod)
    }

    val store = StaticStoreContainer.init(
        File(Values.projectDirectories.dataLocalDir, "/db"),
        "db"
    ).apply {
        initMetaData(XdModel.hierarchy, this)
    }

    fun <T> xodus(block: (TransientStoreSession) -> T) = store.transactional(block = block)

    suspend fun ioTransaction(block: (TransientStoreSession) -> Unit) = coroutineScope {
        launch(Dispatchers.IO) {
            store.transactional(block = block)
        }
    }

    fun getArchiveOrNull(name: String): XdArchive? {
        val archive = XdArchive.query(XdArchive::name eq name).firstOrNull()
        if (archive == null)
            terminal.danger("The given archive '${name}' does not exist!")
        return archive
    }
}
