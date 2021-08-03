package net.axay.pacmc.storage.data

import net.axay.pacmc.data.Repository

class PacmcFile {
    val repository: String
    val modId: String
    val versionId: String
    val index: Int
    val filename: String

    constructor(repository: Repository, modId: String, versionId: String, index: Int) {
        this.repository = repository.stringName
        this.modId = modId
        this.versionId = versionId
        this.index = index
        filename = "pacmc_${repository}_${modId}_${versionId}_${index}.jar"
    }

    constructor(filename: String) {
        val splitName = filename.removeSuffix(".jar").split('_')
        if (splitName.size !in 4..5)
            error("The given filename ($filename) is not a valid pacmc file.")
        repository = splitName[1]
        modId = splitName[2]
        versionId = splitName[3]
        index = splitName.getOrElse(4) { "0" }.toIntOrNull()
            ?: error("The given filename ($filename) is not a valid pacmc file.")
        this.filename = filename
    }
}
