package net.axay.pacmc.storage

import kotlinx.dnq.XdModel
import kotlinx.dnq.query.eq
import kotlinx.dnq.query.firstOrNull
import kotlinx.dnq.query.query
import kotlinx.dnq.store.container.StaticStoreContainer
import kotlinx.dnq.util.initMetaData
import net.axay.pacmc.Values
import net.axay.pacmc.storage.data.XdArchive
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

    fun getArchive(name: String) = store.transactional {
        XdArchive.query(XdArchive::name eq name).firstOrNull()
    }
}
