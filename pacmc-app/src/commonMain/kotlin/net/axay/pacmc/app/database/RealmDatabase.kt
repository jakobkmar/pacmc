package net.axay.pacmc.app.database

import io.realm.Realm
import io.realm.RealmConfiguration
import net.axay.pacmc.app.CommonApplication
import net.axay.pacmc.app.Environment
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.database.model.DbInstalledProject

val realm = run {
    val config = RealmConfiguration.Builder(setOf(DbArchive::class, DbInstalledProject::class))
        .directory(
            Environment.dataLocalDir.resolve("db2").toString()
        )
        .name("pacmc_db")
        .build()
    Realm.open(config)
        .also { CommonApplication.openedRealm = true }
}
