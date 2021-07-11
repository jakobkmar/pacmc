package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import net.axay.pacmc.data.MinecraftVersion
import org.kodein.db.model.Id

@Serializable
class DbArchive(
    @Id val name: String,
    val path: String,
    val gameVersion: String,
) {
    val minecraftVersion by lazy { MinecraftVersion.fromString(gameVersion)!! }
}
