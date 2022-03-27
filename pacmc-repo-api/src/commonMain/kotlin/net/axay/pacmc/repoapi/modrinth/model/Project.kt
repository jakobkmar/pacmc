package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Project(
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
  /**
   * A long form description of the project
   *
   * **Example**: `"A long body describing my project in detail"`
   */
  public val body: String? = null,
  /**
   * An optional link to where to submit bugs or issues with the project
   *
   * **Example**: `"https://github.com/my_user/my_project/issues"`
   */
  @SerialName("issues_url")
  public val issuesUrl: String? = null,
  /**
   * An optional link to the source code of the project
   *
   * **Example**: `"https://github.com/my_user/my_project"`
   */
  @SerialName("source_url")
  public val sourceUrl: String? = null,
  /**
   * An optional link to the project's wiki page or other relevant information
   *
   * **Example**: `"https://github.com/my_user/my_project/wiki"`
   */
  @SerialName("wiki_url")
  public val wikiUrl: String? = null,
  /**
   * An optional invite link to the project's discord
   *
   * **Example**: `"https://discord.gg/AaBbCcDd"`
   */
  @SerialName("discord_url")
  public val discordUrl: String? = null,
  /**
   * A list of donation links for the project
   */
  @SerialName("donation_urls")
  public val donationUrls: List<NonSearchProject.DonationUrls>? = null,
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
   * The ID of the project, encoded as a base62 string
   *
   * **Example**: `"AABBCCDD"`
   */
  public val id: String,
  /**
   * The ID of the team that has ownership of this project
   *
   * **Example**: `"MMNNOOPP"`
   */
  public val team: String,
  /**
   * The link to the long description of the project (only present for old projects)
   *
   * **Example**: `null`
   */
  @SerialName("body_url")
  public val bodyUrl: String? = null,
  /**
   * A message that a moderator sent regarding the project
   *
   * **Example**: `null`
   */
  @SerialName("moderator_message")
  public val moderatorMessage: Project.ModeratorMessage? = null,
  /**
   * The date the project was published
   */
  public val published: Instant,
  /**
   * The date the project was last updated
   */
  public val updated: Instant,
  /**
   * The total number of users following the project
   */
  public val followers: Int,
  /**
   * The status of the project
   *
   * **Example**: `"approved"`
   */
  public val status: Project.Status,
  /**
   * The license of the project
   */
  public val license: Project.License? = null,
  /**
   * A list of the version IDs of the project (will never be empty unless `draft` status)
   *
   * **Example**: `["IIJJKKLL","QQRRSSTT"]`
   */
  public val versions: List<String>? = null,
  /**
   * A list of images that have been uploaded to the project's gallery
   */
  public val gallery: List<Project.Gallery>? = null
) {
  @Serializable
  public data class ModeratorMessage(
    /**
     * The message that a moderator has left for the project
     */
    public val message: String? = null,
    /**
     * The longer body of the message that a moderator has left for the project
     */
    public val body: String? = null
  )

  @Serializable
  public enum class Status {
    @SerialName("approved")
    Approved,
    @SerialName("rejected")
    Rejected,
    @SerialName("draft")
    Draft,
    @SerialName("unlisted")
    Unlisted,
    @SerialName("archived")
    Archived,
    @SerialName("processing")
    Processing,
    @SerialName("unknown")
    Unknown,
  }

  @Serializable
  public data class License(
    /**
     * The license id of a project, retrieved from the licenses get route
     *
     * **Example**: `"lgpl-3"`
     */
    public val id: String? = null,
    /**
     * The long name of a license
     *
     * **Example**: `"GNU Lesser General Public License v3"`
     */
    public val name: String? = null,
    /**
     * The URL to this license
     *
     * **Example**: `"https://cdn.modrinth.com/licenses/lgpl-3.txt"`
     */
    public val url: String? = null
  )

  @Serializable
  public data class Gallery(
    /**
     * The URL of the gallery image
     *
     * **Example**:
     * `"https://cdn.modrinth.com/data/AABBCCDD/images/009b7d8d6e8bf04968a29421117c59b3efe2351a.png"`
     */
    public val url: String,
    /**
     * Whether the image is featured in the gallery
     *
     * **Example**: `true`
     */
    public val featured: Boolean,
    /**
     * The title of the gallery image
     *
     * **Example**: `"My awesome screenshot!"`
     */
    public val title: String? = null,
    /**
     * The description of the gallery image
     *
     * **Example**: `"This awesome screenshot shows all of the blocks in my mod!"`
     */
    public val description: String? = null,
    /**
     * The date and time the gallery image was created
     */
    public val created: Instant
  )
}
