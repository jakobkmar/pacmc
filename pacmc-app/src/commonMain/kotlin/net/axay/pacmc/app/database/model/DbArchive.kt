package net.axay.pacmc.app.database.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.realmListOf
import io.realm.toRealmList
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import okio.Path
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
        path: Path,
        minecraftVersion: MinecraftVersion,
        loader: ModLoader,
        installed: List<DbInstalledProject>,
        color: Int,
    ) : this() {
        this.name = name
        this.displayName = displayName
        this.path = path.toString()
        this.minecraftVersion = minecraftVersion.toString()
        this.loader = loader.name
        this.installed = installed.toRealmList()
        this.color = color
    }
}

class DbInstalledProject() : RealmObject {
    var repository: String = ""
    var id: String = ""
    var dependency: Boolean = false
    var version: String = ""

    fun readModId() = ModId(Repository.valueOf(repository), id)

    // for the current realm compiler plugin
    constructor(
        version: CommonProjectVersion,
        isDependency: Boolean,
    ) : this() {
        this.repository = version.modId.repository.name
        this.id = version.modId.id
        this.dependency = isDependency
        this.version = version.id
    }
}
