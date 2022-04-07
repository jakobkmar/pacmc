package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ProjectResult(
  /**
   * The ID of the project
   *
   * **Example**: `"AABBCCDD"`
   */
  @SerialName("project_id")
  public val projectId: String,
  /**
   * The username of the project's author
   *
   * **Example**: `"my_user"`
   */
  public val author: String,
  /**
   * A list of the minecraft versions supported by the project
   *
   * **Example**: `["1.8","1.8.9"]`
   */
  public val versions: List<String>,
  /**
   * The total number of users following the project
   */
  public val follows: Int,
  /**
   * The date the project was created
   */
  @SerialName("date_created")
  public val dateCreated: Instant,
  /**
   * The date the project was last modified
   */
  @SerialName("date_modified")
  public val dateModified: Instant,
  /**
   * The latest version of minecraft that this project supports
   *
   * **Example**: `"1.8.9"`
   */
  @SerialName("latest_version")
  public val latestVersion: String? = null,
  /**
   * The license of the project
   *
   * **Example**: `"mit"`
   */
  public val license: String,
  /**
   * All gallery images attached to the project
   *
   * **Example**:
   * `["https://cdn.modrinth.com/data/AABBCCDD/images/009b7d8d6e8bf04968a29421117c59b3efe2351a.png","https://cdn.modrinth.com/data/AABBCCDD/images/c21776867afb6046fdc3c21dbcf5cc50ae27a236.png"]`
   */
  public val gallery: List<String>? = null,
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
)
