package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Version(
  /**
   * The ID of the version, encoded as a base62 string
   *
   * **Example**: `"IIJJKKLL"`
   */
  public val id: String,
  /**
   * The ID of the project this version is for
   *
   * **Example**: `"AABBCCDD"`
   */
  @SerialName("project_id")
  public val projectId: String,
  /**
   * The ID of the author who published this version
   *
   * **Example**: `"EEFFGGHH"`
   */
  @SerialName("author_id")
  public val authorId: String,
  @SerialName("date_published")
  public val datePublished: Instant,
  /**
   * The number of times this version has been downloaded
   */
  public val downloads: Int,
  /**
   * A link to the changelog for this version
   *
   * **Example**: `null`
   */
  @SerialName("changelog_url")
  public val changelogUrl: String? = null,
  /**
   * A list of files available for download for this version
   */
  public val files: List<Version.Files>,
  /**
   * The name of this version
   *
   * **Example**: `"Version 1.0.0"`
   */
  public val name: String,
  /**
   * The version number. Ideally will follow semantic versioning
   *
   * **Example**: `"1.0.0"`
   */
  @SerialName("version_number")
  public val versionNumber: String,
  /**
   * The changelog for this version
   *
   * **Example**: `"List of changes in this version: ..."`
   */
  public val changelog: String? = null,
  /**
   * A list of specific versions of projects that this version depends on
   */
  public val dependencies: List<BaseVersion.Dependencies>? = null,
  /**
   * A list of versions of Minecraft that this version supports
   *
   * **Example**: `["1.16.5","1.17.1"]`
   */
  @SerialName("game_versions")
  public val gameVersions: List<String>,
  /**
   * The release channel for this version
   *
   * **Example**: `"release"`
   */
  @SerialName("version_type")
  public val versionType: BaseVersion.VersionType,
  /**
   * The mod loaders that this version supports
   *
   * **Example**: `["fabric","forge"]`
   */
  public val loaders: List<String>,
  /**
   * Whether the version is featured or not
   *
   * **Example**: `true`
   */
  public val featured: Boolean,
) {
  @Serializable
  public data class Files(
    /**
     * A map of hashes of the file. The key is the hashing algorithm and the value is the string
     * version of the hash.
     */
    public val hashes: Files.Hashes,
    /**
     * A direct link to the file
     *
     * **Example**: `"https://cdn.modrinth.com/data/AABBCCDD/versions/1.0.0/my_file.jar"`
     */
    public val url: String,
    /**
     * The name of the file
     *
     * **Example**: `"my_file.jar"`
     */
    public val filename: String,
    /**
     * **Example**: `false`
     */
    public val primary: Boolean,
    /**
     * The size of the file in bytes
     *
     * **Example**: `1097270`
     */
    public val size: Int,
  ) {
    @Serializable
    public data class Hashes(
      /**
       * **Example**:
       * `"93ecf5fe02914fb53d94aa3d28c1fb562e23985f8e4d48b9038422798618761fe208a31ca9b723667a4e05de0d91a3f86bcd8d018f6a686c39550e21b198d96f"`
       */
      public val sha512: String? = null,
      /**
       * **Example**: `"c84dd4b3580c02b79958a0590afd5783d80ef504"`
       */
      public val sha1: String? = null,
    )
  }
}
