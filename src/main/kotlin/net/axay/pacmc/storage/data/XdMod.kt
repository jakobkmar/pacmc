package net.axay.pacmc.storage.data

import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.*

class XdMod(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdMod>()

    var repository by xdRequiredStringProp()
    var id by xdRequiredStringProp()

    var version by xdRequiredStringProp()

    var name by xdRequiredStringProp(trimmed = true)
    var description by xdStringProp(trimmed = true)

    var persistent by xdBooleanProp()

    operator fun component1() = id
    operator fun component2() = name
}
