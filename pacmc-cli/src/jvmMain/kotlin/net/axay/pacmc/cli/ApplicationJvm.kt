package net.axay.pacmc.cli

import kotlin.system.exitProcess

actual suspend fun main(args: Array<String>) {
    try {
        runCli(args)
    } catch (exc: Exception) {
        exc.printStackTrace()
    } finally {
        exitProcess(0) // TODO this shouldn't be necessary but realm currently does not close correctly
    }
}
