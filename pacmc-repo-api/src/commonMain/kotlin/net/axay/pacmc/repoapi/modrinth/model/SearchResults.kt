package net.axay.pacmc.repoapi.modrinth.model

import kotlin.Int
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class SearchResults(
  /**
   * The list of results
   */
  public val hits: List<ProjectResult>,
  /**
   * The number of results that were skipped by the query
   *
   * **Example**: `0`
   */
  public val offset: Int,
  /**
   * The number of results that were returned by the query
   *
   * **Example**: `10`
   */
  public val limit: Int,
  /**
   * The total number of results that match the query
   *
   * **Example**: `10`
   */
  @SerialName("total_hits")
  public val totalHits: Int
)
