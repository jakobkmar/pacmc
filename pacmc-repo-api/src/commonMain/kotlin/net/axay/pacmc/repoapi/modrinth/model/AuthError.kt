package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class AuthError(
  /**
   * The name of the error
   *
   * **Example**: `"unauthorized"`
   */
  public val error: String,
  /**
   * The contents of the error
   *
   * **Example**: `"Authentication Error: Invalid Authentication Credentials"`
   */
  public val description: String,
)
