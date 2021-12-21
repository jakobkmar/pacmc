/**
 * Labrinth
 *
 * This API is documented in the **OpenAPI format** and is available for download [here](/openapi.yaml).  # Cross-Origin Resource Sharing This API features Cross-Origin Resource Sharing (CORS) implemented in compliance with  [W3C spec](https://www.w3.org/TR/cors/). This allows for cross-domain communication from the browser. All responses have a wildcard same-origin which makes them completely public and accessible to everyone, including any code on any site.  # Authentication This API uses GitHub tokens for authentication. The token is in the `Authorization` header of the request. You can get a token [here](#operation/initAuth).    Example:  ```  Authorization: gho_pJ9dGXVKpfzZp4PUHSxYEq9hjk0h288Gwj4S  ``` 
 *
 * The version of the OpenAPI document: 13187de (v2)
 * 
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package net.axay.pacmc.repoapi.modrinth.model


import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * 
 *
 * @param id The user's id
 * @param githubId The user's github id
 * @param avatarUrl The user's avatar url
 * @param created The time at which the user was created
 * @param role The user's role
 */
@Serializable
data class UserAllOf (

    /* The user's id */
    @SerialName(value = "id") val id: kotlin.String? = null,

    /* The user's github id */
    @SerialName(value = "github_id") val githubId: kotlin.Int? = null,

    /* The user's avatar url */
    @SerialName(value = "avatar_url") val avatarUrl: kotlin.String? = null,

    /* The time at which the user was created */
    @SerialName(value = "created") val created: kotlin.String? = null,

    /* The user's role */
    @SerialName(value = "role") val role: UserAllOf.Role? = null

) {

    /**
     * The user's role
     *
     * Values: admin,moderator,developer
     */
    @Serializable
    enum class Role(val value: kotlin.String) {
        @SerialName(value = "admin") admin("admin"),
        @SerialName(value = "moderator") moderator("moderator"),
        @SerialName(value = "developer") developer("developer");
    }
}
