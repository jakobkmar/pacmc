package net.axay.pacmc.storage.data

import kotlinx.serialization.Serializable
import net.axay.pacmc.data.Repository
import net.axay.pacmc.logging.formatMod
import net.axay.pacmc.requests.common.data.CommonModInfo
import org.kodein.db.model.orm.Metadata

@Serializable
data class DbMod(
    val repository: Repository,
    val modId: String,
    val version: String,
    val name: String,
    val slug: String? = null, // never null since model v1 TODO use the @EncodeDefault annotation when it is available
    val author: String? = null, // never null since model v1 TODO use the @EncodeDefault annotation when it is available
    val description: String? = null,
    val persistent: Boolean,
    val archive: String,
) : Metadata {
    override val id get() = listOf(repository, modId, archive)

    val formattedName get() = formatMod(repository, name)

    fun createModInfo() = CommonModInfo(name, slug!!, author!!, description)

    override fun indexes() = mapOf(
        "name" to name,
        "archive" to archive,
        "archiveRepoIdIndex" to listOf(repository, modId, archive),
        "archiveNameIndex" to listOf(name.lowercase(), archive),
        "nameSlugAuthorArchiveIndex" to listOf(name.lowercase(), slug!!.lowercase(), author!!.lowercase(), archive),
        "nameSlugDescriptionArchiveIndex" to listOf(name.lowercase(), slug.lowercase(), description.orEmpty().lowercase(), archive)
    )
}
