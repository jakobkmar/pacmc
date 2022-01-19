package net.axay.pacmc.app.database.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.realmListOf
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModLoader

class DbArchive() : RealmObject {
    @PrimaryKey @Index var name: String = ""
    var displayName: String = ""
    var path: String = ""
    var minecraftVersion: String = ""
    var loader: String = ""
    var installed: RealmList<DbInstalledProject> = realmListOf()
    var color: Int = 0

    fun readMinecraftVersion() = MinecraftVersion.fromString(minecraftVersion)
        ?: error("Invalid minecraft version string in database for archive '$name'")

    fun readLoader() = ModLoader.valueOf(loader)

    // for the current realm compiler plugin
    constructor(
        name: String,
        displayName: String,
        path: String,
        minecraftVersion: MinecraftVersion,
        loader: ModLoader,
        installed: RealmList<DbInstalledProject>,
        color: Int,
    ) : this() {
        this.name = name
        this.displayName = displayName
        this.path = path
        this.minecraftVersion = minecraftVersion.toString()
        this.loader = loader.name
        this.installed = installed
        this.color = color
    }
}

class DbInstalledProject : RealmObject {
    var id: String = ""
}
