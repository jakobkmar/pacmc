package net.axay.pacmc.storage

import com.github.ajalt.mordant.rendering.TextColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.axay.pacmc.Values.dbFile
import net.axay.pacmc.data.Repository
import net.axay.pacmc.storage.data.DbArchive
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.terminal
import org.kodein.db.*
import org.kodein.db.impl.open
import org.kodein.db.model.orm.Metadata

@Serializable
private class DbModelVersion(val version: Int) : Metadata {
    override val id = "dbModelVersion"
}

private const val dbVersion = 1

private inline fun <reified T : Any> DB.insertAgain() {
    find<T>().all().useModels { it.toList() }.forEach { put(it) }
}

val db = DB.open(
    dbFile.canonicalPath,
    ValueConverter.forClass<Repository> { Value.of(it.stringName) }
).apply {
    val currentDbVersion = find<DbModelVersion>().all().use { if (it.isValid()) it.model() else null }?.version

    if (currentDbVersion != dbVersion) {
        put(DbModelVersion(dbVersion))
        terminal.warning("Changed model version of the database to '$dbVersion'")
        terminal.println("This happens after an update of pacmc")

        if (currentDbVersion != null) {
            terminal.print("Migrating data in the database... ")
            insertAgain<DbMod>()
            insertAgain<DbArchive>()
            terminal.println(TextColors.green("done"))
        }

        terminal.println()
    }
}

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
