package net.axay.pacmc.app.database.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.realmListOf
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModLoader

class DbArchive : RealmObject {
    @PrimaryKey @Index var name: String = ""
    var path: String = ""
    var minecraftVersion: String = ""
    var loader: ModLoader = ModLoader.FABRIC
    var installed: RealmList<DbInstalledProject> = realmListOf()

    fun readMinecraftVersion() = MinecraftVersion.fromString(minecraftVersion)
        ?: error("Invalid minecraft version string in database for archive '$name'")
}

class DbInstalledProject : RealmObject {
    var id: String = ""
}
