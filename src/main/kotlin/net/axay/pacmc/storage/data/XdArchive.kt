package net.axay.pacmc.storage.data

import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.XdEntity
import kotlinx.dnq.XdNaturalEntityType
import kotlinx.dnq.xdRequiredStringProp
import net.axay.pacmc.data.MinecraftVersion

class XdArchive(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdArchive>()

    var name by xdRequiredStringProp(unique = true, trimmed = true)
    var path by xdRequiredStringProp(trimmed = true)
    var gameVersion by xdRequiredStringProp(trimmed = true)

    val minecraftVersion by lazy { MinecraftVersion.fromString(gameVersion)!! }
}
