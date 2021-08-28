package net.axay.pacmc.requests.modrinth.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.axay.pacmc.requests.common.data.CommonModInfo
import net.axay.pacmc.requests.modrinth.ModrinthApi

@Serializable
data class ModrinthModInfo(
    val id: String,
    val slug: String? = null,
    @SerialName("team") val teamId: String,
    val title: String,
    val description: String,
) {
    companion object {
        private val resolveScope = CoroutineScope(Dispatchers.Default)
    }

    private val author = resolveScope.async {
        val ownerId = ModrinthApi.getTeamMembers(teamId)!!.first { it.role == ModrinthTeamMember.ROLE_OWNER }.userId
        ModrinthApi.getUser(ownerId)!!.username
    }

    suspend fun convertToCommon() = CommonModInfo(
        title, slug ?: id, author.await(), description
    )
}
