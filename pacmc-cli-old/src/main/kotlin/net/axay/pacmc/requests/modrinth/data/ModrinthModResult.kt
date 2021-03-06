package net.axay.pacmc.requests.modrinth.data

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.axay.pacmc.data.MinecraftVersion
import net.axay.pacmc.data.Repository
import net.axay.pacmc.requests.common.CommonConvertible
import net.axay.pacmc.requests.common.data.CommonModResult

@Serializable
data class ModrinthModResult(
    @SerialName("mod_id") val modId: String,
    val slug: String? = null,
    val author: String,
    val title: String,
    val description: String,
    val versions: List<String>,
    @SerialName("date_modified") val dateModified: Instant,
) : CommonConvertible<CommonModResult> {
    override fun convertToCommon() = CommonModResult(
        Repository.MODRINTH,
        modId.removePrefix("local-"),
        slug ?: modId,
        title,
        description,
        author,
        versions.mapNotNull { MinecraftVersion.fromString(it) },
        dateModified
    )
}
