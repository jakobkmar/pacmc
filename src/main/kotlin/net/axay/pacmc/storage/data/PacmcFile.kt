package net.axay.pacmc.storage.data

class PacmcFile {
    val repository: String
    val modId: Int
    val versionId: Int
    val filename: String

    constructor(repository: String, modId: String, versionId: String) {
        this.repository = repository
        this.modId = modId.toInt()
        this.versionId = versionId.toInt()
        filename = "pacmc_${repository}_${modId}_${versionId}.jar"
    }

    constructor(filename: String) {
        val splitName = filename.removeSuffix(".jar").split('_')
        if (splitName.size != 4)
            error("The given filename ($filename) is not a valid pacmc file.")
        repository = splitName[1]
        modId = splitName[2].toInt()
        versionId = splitName[3].toInt()
        this.filename = filename
    }
}
