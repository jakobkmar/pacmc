package net.axay.pacmc.common.data

sealed class IdOrSlug {
    abstract val repository: Repository
    abstract val idOrSlug: String

    override fun toString() = "${repository.displayName.lowercase()}/$idOrSlug"
}

data class ModId(
    override val repository: Repository,
    val id: String,
) : IdOrSlug() {
    override val idOrSlug get() = id

    override fun toString() = super.toString()
}

data class ModSlug(
    override val repository: Repository,
    val slug: String,
) : IdOrSlug() {
    override val idOrSlug get() = slug

    override fun toString() = super.toString()

    companion object
}
