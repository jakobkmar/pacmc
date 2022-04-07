package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EditableProject(
  /**
   * The status of the project
   *
   * **Example**: `"approved"`
   */
  public val status: EditableProject.Status? = null,
  /**
   * The title of the moderators' message for the project
   */
  @SerialName("moderation_message")
  public val moderationMessage: String? = null,
  /**
   * The body of the moderators' message for the project
   */
  @SerialName("moderation_message_body")
  public val moderationMessageBody: String? = null,
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
}
