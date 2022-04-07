package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class EditableUser(
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
)
