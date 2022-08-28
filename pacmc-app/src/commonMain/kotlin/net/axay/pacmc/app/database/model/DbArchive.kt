package net.axay.pacmc.app.database.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.common.data.*
import okio.Path
import okio.Path.Companion.toPath

class DbArchive() : RealmObject {
    @PrimaryKey
    @Index
    var name: String = ""
    var displayName: String = ""
    var path: String = ""
    var minecraftVersion: String = ""
    var contentType: String = ""
    var loaders: RealmList<String> = realmListOf()
    var installed: RealmList<DbInstalledProject> = realmListOf()
    var color: Int = 0

    fun readPath() = path.toPath()

    fun readGameVersion() = MinecraftVersion.fromString(minecraftVersion)
        ?: error("Invalid minecraft version string in database for archive '$name'")

    fun readLoaders() = loaders.map { ModLoader.valueOf(it) }

    // for the current realm compiler plugin
    constructor(
        name: String,
        displayName: String,
        path: Path,
        minecraftVersion: MinecraftVersion,
        contentType: ContentType,
        loaders: List<ModLoader>,
        installed: List<DbInstalledProject>,
        color: Int,
    ) : this() {
        this.name = name
        this.displayName = displayName
        this.path = path.toString()
        this.minecraftVersion = minecraftVersion.toString()
        this.contentType = contentType.name
        this.loaders = loaders.map { it.name }.toRealmList()
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
