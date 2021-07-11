package net.axay.pacmc.storage

import net.axay.pacmc.Values
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.terminal
import org.kodein.db.DB
import org.kodein.db.getById
import org.kodein.db.impl.open
import java.io.File

val db = DB.open(File(Values.projectDirectories.dataLocalDir, "/db1").canonicalPath)

fun DB.getArchiveOrWarn(name: String) = getById<DbArchive>(name).apply {
    if (this == null)
        terminal.danger("The given archive '${name}' does not exist!")
}
