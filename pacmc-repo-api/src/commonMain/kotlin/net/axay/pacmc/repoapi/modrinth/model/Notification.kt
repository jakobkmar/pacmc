package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Notification(
  /**
   * The id of the notification
   *
   * **Example**: `"UUVVWWXX"`
   */
  public val id: String,
  /**
   * The id of the user who received the notification
   *
   * **Example**: `"EEFFGGHH"`
   */
  @SerialName("user_id")
  public val userId: String,
  /**
   * The type of notification
   *
   * **Example**: `"project_update"`
   */
  public val type: Notification.Type? = null,
  /**
   * The title of the notification
   *
   * **Example**: `"**My Project** has been updated!"`
   */
  public val title: String,
  /**
   * The body text of the notification
   *
   * **Example**: `"The project, My Project, has released a new version: 1.0.0"`
   */
  public val text: String,
  /**
   * A link to the related project or version
   *
   * **Example**: `"mod/AABBCCDD/version/IIJJKKLL"`
   */
  public val link: String,
  /**
   * Whether the notification has been read or not
   *
   * **Example**: `false`
   */
  public val read: Boolean,
  /**
   * The time at which the notification was created
   */
  public val created: Instant,
  /**
   * A list of actions that can be performed
   */
  public val actions: List<Notification.Actions>
) {
  @Serializable
  public enum class Type {
    @SerialName("project_update")
    ProjectUpdate,
    @SerialName("team_invite")
    TeamInvite,
  }

  @Serializable
  public class Actions()
}
