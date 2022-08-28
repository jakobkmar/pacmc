package net.axay.pacmc.repoapi.modrinth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class BaseProject(
    /**
     * The slug of a project, used for vanity URLs
     *
     * **Example**: `"my_project"`
     */
    public val slug: String? = null,
    /**
     * The title or name of the project
     *
     * **Example**: `"My Project"`
     */
    public val title: String? = null,
    /**
     * A short description of the project
     *
     * **Example**: `"A short description"`
     */
    public val description: String? = null,
    /**
     * A list of the categories that the project is in
     *
     * **Example**: `["technology","adventure","fabric"]`
     */
    public val categories: List<String>? = null,
    /**
     * The client side support of the project
     *
     * **Example**: `"required"`
     */
    @SerialName("client_side") public val clientSide: BaseProject.ClientSide? = null,
    /**
     * The server side support of the project
     *
     * **Example**: `"optional"`
     */
    @SerialName("server_side") public val serverSide: BaseProject.ServerSide? = null,
) {
    @Serializable
    public enum class ClientSide {
        @SerialName("required")
        Required,

        @SerialName("optional")
        Optional,

        @SerialName("unsupported")
        Unsupported,
    }

    @Serializable
    public enum class ServerSide {
        @SerialName("required")
        Required,

        @SerialName("optional")
        Optional,

        @SerialName("unsupported")
        Unsupported
    }
}
