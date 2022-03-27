package net.axay.pacmc.repoapi.modrinth.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CategoryTagArrayElement(
  /**
   * The SVG icon of a category
   *
   * **Example**: `"<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\"
   * stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\"
   * r=\"10\"/><polygon points=\"16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76\"/></svg>"`
   */
  public val icon: String,
  /**
   * The name of the category
   *
   * **Example**: `"adventure"`
   */
  public val name: String,
  /**
   * The project type this category is applicable to
   *
   * **Example**: `"mod"`
   */
  @SerialName("project_type")
  public val projectType: String
)

public typealias CategoryTag = List<CategoryTagArrayElement>
