package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class DonationPlatformTag(
  /**
   * The short identifier of the donation platform
   *
   * **Example**: `"bmac"`
   */
  public val short: String,
  /**
   * The full name of the donation platform
   *
   * **Example**: `"Buy Me a Coffee"`
   */
  public val name: String,
)
