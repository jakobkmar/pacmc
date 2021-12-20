package net.axay.pacmc.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.axay.pacmc.Values.dbFile
import net.axay.pacmc.data.Repository
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.terminal
import org.kodein.db.*
import org.kodein.db.impl.open

val db = DB.open(
    dbFile.canonicalPath,
    ValueConverter.forClass<Repository> { Value.of(it.stringName) }
).migrateDatabase()

suspend fun DB.execAsyncBatch(block: ExecBatch.() -> Unit) = coroutineScope {
    launch(Dispatchers.IO) {
        execBatch(block = block)
    }
}

fun DB.getArchiveOrWarn(name: String) = getById<DbArchive>(name).apply {
    if (this == null)
        terminal.danger("The given archive '${name}' does not exist!")
}

fun DB.getArchiveMods(archiveName: String) =
    find<DbMod>().byIndex("archive", archiveName).useModels { it.toList() }
