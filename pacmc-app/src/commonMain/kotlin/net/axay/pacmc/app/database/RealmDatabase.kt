package net.axay.pacmc.app.database

import io.realm.Realm
import io.realm.RealmConfiguration
import net.axay.pacmc.app.Environment

val realm = kotlin.run {
    val config = RealmConfiguration.Builder()
        .path(Environment.dataLocalDir.resolve("/db2").toString())
        .schema()
        .build()
    Realm.open(config)
}
