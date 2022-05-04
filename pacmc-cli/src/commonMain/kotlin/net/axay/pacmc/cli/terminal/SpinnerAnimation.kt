package net.axay.pacmc.cli.terminal

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.*
import net.axay.pacmc.app.utils.OperatingSystem
import net.axay.pacmc.cli.terminal

class SpinnerAnimation : CoroutineScope {
    override val coroutineContext = Dispatchers.Default

    private val currentMessage = atomic("resolving")

    private val spinJob = launch(start = CoroutineStart.LAZY) {
        val symbols = listOf('|', '/', '-', '\\')
        var index = 0
        while (isActive) {
            terminal.print("[${symbols[index]}] ")
            terminal.cursor.move { clearLineAfterCursor() }
            terminal.print(currentMessage.value)
            terminal.cursor.move { startOfLine() }
            index = if (index < symbols.lastIndex) index + 1 else 0
            delay(100)
        }
    }

    fun start() {
        spinJob.start()
    }

    suspend fun stop() {
        spinJob.cancelAndJoin()
        terminal.cursor.move { startOfLine() }
        terminal.print("[${if (OperatingSystem.notWindows) '✓' else '+'}] ")
        terminal.cursor.move { clearLineAfterCursor() }
        terminal.println("done")
    }

    fun update(message: String) {
        currentMessage.update { message }
    }
}
