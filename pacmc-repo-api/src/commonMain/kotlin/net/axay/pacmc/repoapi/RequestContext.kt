package net.axay.pacmc.repoapi

class RequestContext(
    val cachePolicy: CachePolicy,
) {
    enum class CachePolicy {
        ONLY_CACHED, CACHED_OR_FRESH, ONLY_FRESH;
    }
}
