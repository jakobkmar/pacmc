package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Int
import kotlin.String
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class User(
  /**
   * The user's id
   *
   * **Example**: `"EEFFGGHH"`
   */
  public val id: String,
  /**
   * The user's github id
   *
   * **Example**: `11223344`
   */
  @SerialName("github_id")
  public val githubId: Int,
  /**
   * The user's avatar url
   *
   * **Example**: `"https://avatars.githubusercontent.com/u/11223344?v=1"`
   */
  @SerialName("avatar_url")
  public val avatarUrl: String,
  /**
   * The time at which the user was created
   */
  public val created: Instant,
  /**
   * The user's role
   *
   * **Example**: `"developer"`
   */
  public val role: User.Role,
  /**
   * The user's username
   *
   * **Example**: `"my_user"`
   */
  public val username: String,
  /**
   * The user's display name
   *
   * **Example**: `"My User"`
   */
  public val name: String? = null,
  /**
   * The user's email (only your own is ever displayed)
   */
  public val email: String? = null,
  /**
   * A description of the user
   *
   * **Example**: `"My short biography"`
   */
  public val bio: String? = null,
) {
  @Serializable
  public enum class Role {
    @SerialName("admin")
    Admin,
    @SerialName("moderator")
    Moderator,
    @SerialName("developer")
    Developer,
  }
}
