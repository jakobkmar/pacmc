package net.axay.pacmc.storage.data

import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.*

class XdMod(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdMod>()

    var name by xdRequiredStringProp(trimmed = true)
    var id by xdRequiredIntProp()

    var persistent by xdBooleanProp()
}
