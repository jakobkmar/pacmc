package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class BaseVersion(
  /**
   * The name of this version
   *
   * **Example**: `"Version 1.0.0"`
   */
  public val name: String? = null,
  /**
   * The version number. Ideally will follow semantic versioning
   *
   * **Example**: `"1.0.0"`
   */
  @SerialName("version_number")
  public val versionNumber: String? = null,
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
  public val gameVersions: List<String>? = null,
  /**
   * The release channel for this version
   *
   * **Example**: `"release"`
   */
  @SerialName("version_type")
  public val versionType: BaseVersion.VersionType? = null,
  /**
   * The mod loaders that this version supports
   *
   * **Example**: `["fabric","forge"]`
   */
  public val loaders: List<String>? = null,
  /**
   * Whether the version is featured or not
   *
   * **Example**: `true`
   */
  public val featured: Boolean? = null,
) {
  @Serializable
  public data class Dependencies(
    /**
     * The ID of the version that this version depends on
     *
     * **Example**: `"IIJJKKLL"`
     */
    @SerialName("version_id")
    public val versionId: String? = null,
    /**
     * The ID of the project that this version depends on
     *
     * **Example**: `"QQRRSSTT"`
     */
    @SerialName("project_id")
    public val projectId: String? = null,
    /**
     * The type of dependency that this version has
     *
     * **Example**: `"required"`
     */
    @SerialName("dependency_type")
    public val dependencyType: Dependencies.DependencyType,
  ) {
    @Serializable
    public enum class DependencyType {
      @SerialName("required")
      Required,
      @SerialName("optional")
      Optional,
      @SerialName("incompatible")
      Incompatible,
      @SerialName("embedded")
      Embedded,
    }
  }

  @Serializable
  public enum class VersionType {
    @SerialName("release")
    Release,
    @SerialName("beta")
    Beta,
    @SerialName("alpha")
    Alpha,
  }
}
