package net.axay.pacmc.repoapi.modrinth.model

import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class ProjectDependencyList(
  /**
   * Projects that the project depends upon
   */
  public val projects: List<Project>? = null,
  /**
   * Versions that the project depends upon
   */
  public val versions: List<Version>? = null
)
