package net.axay.pacmc.app.database

import io.realm.Realm
import io.realm.RealmConfiguration
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.database.model.DbInstalledProject

val realm = run {
    val config = RealmConfiguration.Builder()
        .path(
            Environment.dataLocalDir.resolve("db2/pacmc_db").also {
                Environment.fileSystem.createDirectories(it.parent!!)
            }.toString()
        )
        .schema(DbArchive::class, DbInstalledProject::class)
        .build()
    Realm.open(config)
}
