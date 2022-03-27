package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GameVersionTagArrayElement(
  /**
   * The name/number of the game version
   *
   * **Example**: `"1.18.1"`
   */
  public val version: String,
  /**
   * The type of the game version
   *
   * **Example**: `"release"`
   */
  @SerialName("version_type")
  public val versionType: GameVersionTagArrayElement.VersionType,
  /**
   * The date of the game version release
   */
  public val date: Instant,
  /**
   * Whether or not this is a major version, used for Featured Versions
   *
   * **Example**: `true`
   */
  public val major: Boolean
) {
  @Serializable
  public enum class VersionType {
    @SerialName("release")
    Release,
    @SerialName("snapshot")
    Snapshot,
    @SerialName("alpha")
    Alpha,
    @SerialName("beta")
    Beta,
  }
}

public typealias GameVersionTag = List<GameVersionTagArrayElement>
