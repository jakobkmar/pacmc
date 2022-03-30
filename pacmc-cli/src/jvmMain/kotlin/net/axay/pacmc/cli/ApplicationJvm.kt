package net.axay.pacmc.cli

import kotlin.system.exitProcess

actual suspend fun main(args: Array<String>) {
    runCli(args)
    exitProcess(1) // TODO this shouldn't be necessary but realm currently does not close correctly
}
