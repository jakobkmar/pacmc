package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata

@Serializable
data class DbMod(
    val repository: String,
    val modId: String,
    val version: String,
    val name: String,
    val description: String?,
    val persistent: Boolean,
    val archive: String,
) : Metadata {
    override val id get() = listOf(repository, modId, archive)

    override fun indexes() = mapOf(
        "name" to name,
        "archive" to archive,
        "archiveRepoIdIndex" to listOf(repository, modId, archive),
        "archiveNameIndex" to listOf(name, archive)
    )
}
