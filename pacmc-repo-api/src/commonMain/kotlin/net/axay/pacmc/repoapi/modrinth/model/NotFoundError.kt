package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class NotFoundError(
  /**
   * The name of the error
   *
   * **Example**: `"not_found"`
   */
  public val error: String,
  /**
   * The contents of the error
   *
   * **Example**: `"the requested route does not exist"`
   */
  public val description: String,
)
