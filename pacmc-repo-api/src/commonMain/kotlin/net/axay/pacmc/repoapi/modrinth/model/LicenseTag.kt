package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class LicenseTagArrayElement(
  /**
   * The short identifier of the license
   *
   * **Example**: `"lgpl-3"`
   */
  public val short: String,
  /**
   * The full name of the license
   *
   * **Example**: `"GNU Lesser General Public License v3"`
   */
  public val name: String
)

public typealias LicenseTag = List<LicenseTagArrayElement>
