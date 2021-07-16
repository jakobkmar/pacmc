package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import net.axay.pacmc.data.MinecraftVersion
import org.kodein.db.model.orm.Metadata
import java.io.File

@Serializable
data class DbArchive(
    val name: String,
    val path: String,
    val gameVersion: String,
) : Metadata {
    override val id get() = name
    override fun indexes() = mapOf("path" to path)

    val minecraftVersion by lazy { MinecraftVersion.fromString(gameVersion)!! }

    val directory get() = File(path)

    val files get() = javaFiles.map { it to PacmcFile(it.name) }
    val javaFiles get() = (directory.listFiles() ?: emptyArray()).filter { it.name.startsWith("pacmc_") }
    val pacmcFiles get() = javaFiles.map { PacmcFile(it.name) }
}
