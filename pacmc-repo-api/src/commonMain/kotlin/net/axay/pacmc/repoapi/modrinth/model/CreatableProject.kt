package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CreatableProject(
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
   * The license ID of a project, retrieved from the license tag route
   *
   * **Example**: `"lgpl-3"`
   */
  @SerialName("license_id")
  public val licenseId: String? = null,
  /**
   * The URL to this license
   *
   * **Example**: `"https://cdn.modrinth.com/licenses/lgpl-3.txt"`
   */
  @SerialName("license_url")
  public val licenseUrl: String? = null,
  /**
   * **Example**: `"modpack"`
   */
  @SerialName("project_type")
  public val projectType: CreatableProject.ProjectType,
  /**
   * A list of initial versions to upload with the created project (required unless `is_draft` is
   * true)
   */
  @SerialName("initial_versions")
  public val initialVersions: List<EditableVersion>? = null,
  /**
   * Whether the project should be saved as a draft instead of being sent to moderation for review
   *
   * **Example**: `true`
   */
  @SerialName("is_draft")
  public val isDraft: Boolean? = null,
  @SerialName("gallery_items")
  public val galleryItems: List<CreatableProject.GalleryItems>? = null
) {
  @Serializable
  public enum class ProjectType {
    @SerialName("mod")
    Mod,
    @SerialName("modpack")
    Modpack,
  }

  @Serializable
  public data class GalleryItems(
    /**
     * The name of the multipart item where the gallery media is located
     */
    public val item: String? = null,
    /**
     * Whether the image is featured in the gallery
     *
     * **Example**: `true`
     */
    public val featured: Boolean? = null,
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
    public val description: String? = null
  )
}
