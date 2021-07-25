package net.axay.pacmc.requests.curse.data

import kotlinx.serialization.Serializable

@Serializable
class CurseProxyProjectInfo(val name: String, val summary: String? = null)
