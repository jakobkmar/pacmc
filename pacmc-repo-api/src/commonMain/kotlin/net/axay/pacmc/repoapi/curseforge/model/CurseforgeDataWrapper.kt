package net.axay.pacmc.repoapi.curseforge.model

import kotlinx.serialization.Serializable

@Serializable
data class CurseforgeDataWrapper<T>(val data: T)
