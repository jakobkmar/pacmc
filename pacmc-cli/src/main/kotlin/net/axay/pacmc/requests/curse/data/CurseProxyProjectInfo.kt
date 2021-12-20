package net.axay.pacmc.requests.curse.data

import kotlinx.serialization.Serializable
import net.axay.pacmc.requests.common.CommonConvertible
import net.axay.pacmc.requests.common.data.CommonModInfo

@Serializable
data class CurseProxyProjectInfo(
    val name: String,
    val authors: List<CurseProxyProject.Author>,
    val summary: String? = null,
    val slug: String,
) : CommonConvertible<CommonModInfo> {
    override fun convertToCommon() = CommonModInfo(
        name, slug, authors.first().name, summary
    )
}
