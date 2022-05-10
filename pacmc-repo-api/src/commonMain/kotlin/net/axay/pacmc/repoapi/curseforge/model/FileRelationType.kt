package net.axay.pacmc.repoapi.curseforge.model

enum class FileRelationType {
    EMBEDDED_LIBRARY,
    OPTIONAL_DEPENDENCY,
    REQUIRED_DEPENDENCY,
    TOOL,
    INCOMPATIBLE,
    INCLUDE,
}
