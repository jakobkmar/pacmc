package net.axay.pacmc.storage.data

class PacmcFile {
    val repository: String
    val modId: String
    val versionId: String
    val filename: String

    constructor(repository: String, modId: String, versionId: String) {
        this.repository = repository
        this.modId = modId
        this.versionId = versionId
        filename = "pacmc_${repository}_${modId}_${versionId}.jar"
    }

    constructor(filename: String) {
        val splitName = filename.split('_')
        if (splitName.size != 4)
            error("The given filename ($filename) is not a valid pacmc file.")
        repository = splitName[1]
        modId = splitName[2]
        versionId = splitName[3]
        this.filename = filename
    }
}
