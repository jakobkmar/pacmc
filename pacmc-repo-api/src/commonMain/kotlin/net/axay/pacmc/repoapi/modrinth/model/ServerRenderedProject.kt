package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ServerRenderedProject(
  /**
   * The project type of the project
   *
   * **Example**: `"mod"`
   */
  @SerialName("project_type")
  public val projectType: ServerRenderedProject.ProjectType,
  /**
   * The total number of downloads of the project
   */
  public val downloads: Int,
  /**
   * The URL of the project's icon
   *
   * **Example**:
   * `"https://cdn.modrinth.com/data/AABBCCDD/b46513nd83hb4792a9a0e1fn28fgi6090c1842639.png"`
   */
  @SerialName("icon_url")
  public val iconUrl: String? = null,
  /**
   * The slug of a project, used for vanity URLs
   *
   * **Example**: `"my_project"`
   */
  public val slug: String? = null,
  /**
   * The title or name of the project
   *
   * **Example**: `"My Project"`
   */
  public val title: String? = null,
  /**
   * A short description of the project
   *
   * **Example**: `"A short description"`
   */
  public val description: String? = null,
  /**
   * A list of the categories that the project is in
   *
   * **Example**: `["technology","adventure","fabric"]`
   */
  public val categories: List<String>? = null,
  /**
   * The client side support of the project
   *
   * **Example**: `"required"`
   */
  @SerialName("client_side")
  public val clientSide: BaseProject.ClientSide? = null,
  /**
   * The server side support of the project
   *
   * **Example**: `"optional"`
   */
  @SerialName("server_side")
  public val serverSide: BaseProject.ServerSide? = null,
) {
  @Serializable
  public enum class ProjectType {
    @SerialName("mod")
    Mod,
    @SerialName("modpack")
    Modpack,
    @SerialName("resourcepack")
    Resourcepack,
    @SerialName("plugin")
    Plugin
  }
}
