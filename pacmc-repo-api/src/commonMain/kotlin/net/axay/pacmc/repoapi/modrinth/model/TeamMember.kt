package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TeamMember(
  /**
   * The ID of the team this team member is a member of
   *
   * **Example**: `"MMNNOOPP"`
   */
  @SerialName("team_id")
  public val teamId: String,
  public val user: User,
  /**
   * The user's role on the team
   *
   * **Example**: `"Member"`
   */
  public val role: String,
  /**
   * The user's permissions in bitflag format (requires authorization to view)
   *
   * In order from first to eighth bit, the bits are:
   * - UPLOAD_VERSION
   * - DELETE_VERSION
   * - EDIT_DETAILS
   * - EDIT_BODY
   * - MANAGE_INVITES
   * - REMOVE_MEMBER
   * - EDIT_MEMBER
   * - DELETE_PROJECT
   *
   *
   * **Example**: `127`
   */
  public val permissions: Int? = null,
  /**
   * Whether or not the user has accepted to be on the team (requires authorization to view)
   *
   * **Example**: `true`
   */
  public val accepted: Boolean
)
