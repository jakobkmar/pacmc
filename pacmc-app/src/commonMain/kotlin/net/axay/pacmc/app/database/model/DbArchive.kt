package net.axay.pacmc.app.database.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.realmListOf
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.ModLoader
import okio.Path.Companion.toPath

class DbArchive() : RealmObject {
    @PrimaryKey @Index var name: String = ""
    var displayName: String = ""
    var path: String = ""
    var minecraftVersion: String = ""
    var loader: String = ""
    var installed: RealmList<DbInstalledProject> = realmListOf()
    var color: Int = 0

    fun readPath() = path.toPath()

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

class DbInstalledProject() : RealmObject {
    var repository: String = ""
    var id: String = ""
    var dependency: Boolean = false

    fun matches(modId: ModId) = modId.id == id && modId.repository.name == repository

    // for the current realm compiler plugin
    constructor(
        modId: ModId,
        dependency: Boolean,
    ) : this() {
        this.repository = modId.repository.name
        this.id = modId.id
        this.dependency = dependency
    }
}
