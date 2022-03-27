package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class DonationPlatformTagArrayElement(
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
  public val name: String
)

public typealias DonationPlatformTag = List<DonationPlatformTagArrayElement>
