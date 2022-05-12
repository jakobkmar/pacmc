package net.axay.pacmc.app.repoapi

import net.axay.pacmc.common.data.ModId
import net.axay.pacmc.common.data.ModSlug
import net.axay.pacmc.repoapi.CachePolicy

suspend fun ModSlug.resolveId(): ModId? {
    return repoApiContext(CachePolicy.ONLY_FRESH) { c -> c.getBasicProjectInfo(this@resolveId) }?.id
}
