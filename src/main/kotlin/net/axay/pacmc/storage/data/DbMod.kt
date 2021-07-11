package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import org.kodein.db.model.Indexed
import org.kodein.db.model.orm.Metadata

@Serializable
data class DbMod(
    val repository: String,
    val modid: String,
    val version: String,
    @Indexed("name") val name: String,
    val description: String,
    val persistent: Boolean,
) : Metadata {
    override val id get() = listOf(repository, modid)
}
