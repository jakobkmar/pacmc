package net.axay.pacmc.storage

import kotlinx.dnq.XdModel
import kotlinx.dnq.query.eq
import kotlinx.dnq.query.firstOrNull
import kotlinx.dnq.query.query
import kotlinx.dnq.store.container.StaticStoreContainer
import kotlinx.dnq.util.initMetaData
import net.axay.pacmc.Values
import net.axay.pacmc.storage.data.XdArchive
import net.axay.pacmc.terminal
import java.io.File

object Xodus {
    init {
        XdModel.registerNodes(XdArchive)
    }

    val store = StaticStoreContainer.init(
        File(Values.projectDirectories.dataLocalDir, "/db"),
        "db"
    ).apply {
        initMetaData(XdModel.hierarchy, this)
    }

    /**
     * Loads archive data from the database.
     *
     * @return a pair, where the first element is the path, the second one is
     * the minecraft version
     */
    fun getArchive(name: String) = store.transactional {
        val archive = XdArchive.query(XdArchive::name eq name).firstOrNull()
        if (archive == null) {
            terminal.danger("The given archive '${name}' does not exist!")
            null
        } else {
            archive.path to archive.minecraftVersion
        }
    }
}
