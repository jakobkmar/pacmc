package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CreatableReport(
  /**
   * The type of the report being sent
   *
   * **Example**: `"copyright"`
   */
  @SerialName("report_type")
  public val reportType: String,
  /**
   * The ID of the item (project, version, or user) being reported
   *
   * **Example**: `"EEFFGGHH"`
   */
  @SerialName("item_id")
  public val itemId: String,
  /**
   * The type of the item being reported
   *
   * **Example**: `"project"`
   */
  @SerialName("item_type")
  public val itemType: CreatableReport.ItemType,
  /**
   * The extended explanation of the report
   *
   * **Example**: `"This is a reupload of my mod, AABBCCDD!"`
   */
  public val body: String,
) {
  @Serializable
  public enum class ItemType {
    @SerialName("project")
    Project,
    @SerialName("user")
    User,
    @SerialName("version")
    Version,
  }
}
