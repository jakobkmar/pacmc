package net.axay.pacmc.requests.modrinth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.axay.pacmc.requests.common.data.CommonModInfo

@Serializable
data class ModrinthModInfo(
    @SerialName("slug") override val name: String,
    override val description: String,
) : CommonModInfo()
