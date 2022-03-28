package net.axay.pacmc.cli

import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.axay.pacmc.cli.commands.Pacmc
import kotlin.coroutines.CoroutineContext

private lateinit var mainScope: CoroutineScope

val terminal = Terminal()

expect suspend fun main(args: Array<String>)

internal suspend fun runCli(args: Array<String>) {
    coroutineScope {
        mainScope = this
        Pacmc().main(args)
    }
}

fun launchJob(
    context: CoroutineContext = Dispatchers.Default,
    block: suspend CoroutineScope.() -> Unit
) {
    mainScope.launch(context, block = block)
}
