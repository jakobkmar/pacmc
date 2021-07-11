package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import org.kodein.db.model.Indexed
import org.kodein.db.model.orm.Metadata

@Serializable
data class DbMod(
    val repository: String,
    val modId: String,
    val version: String,
    @Indexed("name") val name: String,
    val description: String?,
    val persistent: Boolean,
    @Indexed("archive") val archive: String,
) : Metadata {
    override val id get() = listOf(repository, modId, archive)

    @Indexed("archiveRepoIdIndex") fun archiveRepoIdIndex() = listOf(repository, modId, archive)
    @Indexed("archiveNameIndex") fun archiveNameIndex() = listOf(name, archive)
}
