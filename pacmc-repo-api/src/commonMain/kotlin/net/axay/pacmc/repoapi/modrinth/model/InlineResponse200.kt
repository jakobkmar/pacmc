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

import net.axay.pacmc.repoapi.modrinth.model.ProjectResult

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * 
 *
 * @param hits The list of results
 * @param offset The number of results that were skipped by the query
 * @param limit The number of results that were returned by the query
 * @param totalHits The total number of results that match the query
 */
@Serializable
data class InlineResponse200 (

    /* The list of results */
    @SerialName(value = "hits") val hits: kotlin.collections.List<ProjectResult>? = null,

    /* The number of results that were skipped by the query */
    @SerialName(value = "offset") val offset: kotlin.Int? = null,

    /* The number of results that were returned by the query */
    @SerialName(value = "limit") val limit: kotlin.Int? = null,

    /* The total number of results that match the query */
    @SerialName(value = "total_hits") val totalHits: kotlin.Int? = null

)
