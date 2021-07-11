package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import net.axay.pacmc.data.MinecraftVersion
import org.kodein.db.model.Id
import java.io.File

@Serializable
class DbArchive(
    @Id val name: String,
    val path: String,
    val gameVersion: String,
) {
    val minecraftVersion by lazy { MinecraftVersion.fromString(gameVersion)!! }

    val directory get() = File(path)

    val files get() = (directory.listFiles() ?: emptyArray()).filter { it.name.startsWith("pacmc_") }
    val pacmcFiles get() = files.map { PacmcFile(it.name) }
}
