package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.axay.pacmc.cli.terminal
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val PROGRESS_BAR_WIDTH = 30

class DownloadAnimation {
    private var maxWidth = 0

    private val finished = LinkedHashMap<String, AnimationState>()
    private val inProgress = LinkedHashMap<String, AnimationState>()

    private val updateMutex = Mutex()

    class AnimationState(
        val progress: Double,
        val message: String? = null,
        val color: TextColors? = null,
    )

    suspend fun update(item: String, state: AnimationState) {
        updateMutex.withLock {
            terminal.cursor.move { up(finished.size + inProgress.size) }

            if (state.progress >= 1) {
                inProgress -= item
                finished[item] = state
            } else {
                inProgress[item] = state
            }
            maxWidth = max(maxWidth, item.length)

            printAll()
        }
    }

    private fun printAll() {
        finished.forEach { (text, state) -> printProgress(text, state) }
        inProgress.forEach { (text, state) -> printProgress(text, state) }
    }

    private fun printProgress(text: String, state: AnimationState) {
        val progress = state.progress

        val lineWidth = max(min((progress * PROGRESS_BAR_WIDTH).roundToInt(), PROGRESS_BAR_WIDTH), 0)
        val string = buildString {
            append(TextColors.brightCyan(text.padStart(maxWidth + 1, ' ')))
            append(" [")
            if (lineWidth > 0) {
                val color = state.color ?: (if (progress >= 1) TextColors.green else TextColors.brightYellow)
                append(color("─".repeat(lineWidth - 1) + ">"))
            }
            append(" ".repeat(PROGRESS_BAR_WIDTH - lineWidth))
            append("] ")
        }

        terminal.print(string)
        terminal.cursor.move { clearLineAfterCursor() }
        terminal.println(when {
            state.message != null -> state.message
            progress < 1 -> TextColors.brightWhite("${(progress * 100).roundToInt()}%")
            else -> TextColors.brightGreen(TextStyles.bold("done"))
        })
    }
}
