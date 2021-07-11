package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import org.kodein.db.model.Id
import org.kodein.db.model.Indexed

@Serializable
data class DbMod(
    @Id val uid: String,
    val repository: String,
    val modId: String,
    val version: String,
    @Indexed("name") val name: String,
    val description: String?,
    val persistent: Boolean,
)
