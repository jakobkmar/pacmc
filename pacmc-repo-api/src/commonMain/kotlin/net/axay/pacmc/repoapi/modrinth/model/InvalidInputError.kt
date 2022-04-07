package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class InvalidInputError(
  /**
   * The name of the error
   *
   * **Example**: `"invalid_input"`
   */
  public val error: String,
  /**
   * The contents of the error
   *
   * **Example**: `"Error while parsing multipart payload"`
   */
  public val description: String,
)
