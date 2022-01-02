package net.axay.pacmc.app.data

sealed class IdOrSlug {
    abstract val repository: Repository
    abstract val idOrSlug: String
}

data class ModId(
    override val repository: Repository,
    val id: String,
) : IdOrSlug() {
    override val idOrSlug get() = id
}

data class ModSlug(
    override val repository: Repository,
    val slug: String,
) : IdOrSlug() {
    override val idOrSlug get() = slug
}
