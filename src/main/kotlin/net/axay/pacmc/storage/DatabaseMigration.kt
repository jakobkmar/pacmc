package net.axay.pacmc.storage

import com.github.ajalt.mordant.rendering.TextColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.axay.pacmc.Values
import net.axay.pacmc.data.Repository
import net.axay.pacmc.requests.common.RepositoryApi
import net.axay.pacmc.requests.common.data.CommonModInfo
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.terminal
import org.kodein.db.DB
import org.kodein.db.find
import org.kodein.db.model.orm.Metadata
import org.kodein.db.useModels

@Serializable
private class DbModelVersion(val version: Int) : Metadata {
    override val id = "dbModelVersion"
}

private class DbModelMigrationStep(val from: Int?, val to: Int?) {
    fun matches(step: Pair<Int, Int>) = (from == null || from == step.first) && (to == null || to == step.second)
}

private const val dbVersion = 1

// noinline because of https://youtrack.jetbrains.com/issue/KT-48353
private inline fun <reified T : Any> DB.insertAgain(noinline mutator: suspend CoroutineScope.(T) -> T = { it }) {
    runBlocking(Dispatchers.Default) {
        find<T>().all().useModels { it.toList() }
            .map { async { mutator(it) } }
            .forEach { put(it.await()) }
    }
}

fun DB.migrateDatabase() = apply {
    val currentDbVersion = find<DbModelVersion>().all().use { if (it.isValid()) it.model() else null }?.version

    if (currentDbVersion != dbVersion) {
        put(DbModelVersion(dbVersion))
        terminal.warning("Changed model version of the database to '$dbVersion'")
        terminal.println("This happens after an update of pacmc")

        if ((currentDbVersion?.compareTo(dbVersion) ?: 0) < 0)
            terminal.danger("The current version of the database was greater than the new version, skipping migrations...")
        else {
            terminal.println("Migrating data in the database... ")
            val currentStep = (currentDbVersion ?: 0) to dbVersion
            for ((step, logic) in migrations) {
                if (step.matches(currentStep)) logic(this@migrateDatabase)
            }
            terminal.println("Migrating data in the database... " + TextColors.green("done"))
        }

        terminal.println()
    }
}

private val migrations: Map<DbModelMigrationStep, DB.() -> Unit> by lazy {
    mapOf(
        DbModelMigrationStep(0, 1) to {
            insertAgain<DbMod>()
        },
    )
}

object DatabaseMigration {
    inline fun <T> migrateMissingModInfoValue(
        repository: Repository,
        modId: String,
        name: String,
        valueName: String,
        crossinline valueGetter: (CommonModInfo) -> T,
    ): T = runBlocking(Dispatchers.Default) {
        val newValue = RepositoryApi.getModInfo(modId, repository)?.let(valueGetter)
        if (newValue != null)
            terminal.println("Resolved the $valueName '$newValue' for '${repository}/${name}'")
        else {
            terminal.danger("FATAL: Could not resolve the $valueName for '${repository}/${name}'")
            terminal.danger("Clear the pacmc dataLocalDir (${Values.dataLocalDir.canonicalPath}) on your disk and recreate your mod archives")
        }

        newValue ?: error("Couldn't resolve the $valueName for a mod in the database")
    }
}
