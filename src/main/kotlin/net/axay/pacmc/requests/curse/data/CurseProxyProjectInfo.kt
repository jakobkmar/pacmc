package net.axay.pacmc.requests.curse.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.axay.pacmc.requests.common.data.CommonModInfo

@Serializable
class CurseProxyProjectInfo(
    override val name: String,
    @SerialName("summary") override val description: String? = null
) : CommonModInfo() {
    override val slug: String? = null
}
