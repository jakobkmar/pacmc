package net.axay.pacmc.requests.modrinth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthTeamMember(
    @SerialName("team_id") val teamId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
    val accepted: Boolean,
) {
    companion object {
        const val ROLE_OWNER = "Owner"
        const val ROLE_MEMBER = "Member"
    }
}
