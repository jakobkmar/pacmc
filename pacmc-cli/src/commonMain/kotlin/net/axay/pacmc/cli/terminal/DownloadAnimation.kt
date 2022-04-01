package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import net.axay.pacmc.cli.terminal
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val PROGRESS_BAR_WIDTH = 30

class DownloadAnimation {
    private var maxWidth = 0

    private val finished = LinkedHashSet<String>()
    private val inProgress = LinkedHashMap<String, Double>()

    fun update(item: String, progress: Double) {
        terminal.cursor.move { up(finished.size + inProgress.size) }

        if (progress >= 1) {
            inProgress -= item
            finished += item
        } else {
            inProgress[item] = progress
        }
        maxWidth = max(maxWidth, item.length)

        printAll()
    }

    private fun printAll() {
        for (text in finished) {
            printProgress(text, 1.0)
        }

        inProgress.forEach { (text, progress) -> printProgress(text, progress) }
    }

    private fun printProgress(text: String, progress: Double) {

        val lineWidth = max(min((progress * PROGRESS_BAR_WIDTH).roundToInt(), PROGRESS_BAR_WIDTH), 0)
        val string = buildString {
            append(TextColors.brightCyan(text.padEnd(maxWidth, ' ')))
            append(" [")
            if (lineWidth > 0) {
                val color = if (progress >= 1) TextColors.green else TextColors.brightYellow
                append(color("─".repeat(lineWidth - 1) + ">"))
            }
            append(" ".repeat(PROGRESS_BAR_WIDTH - lineWidth))
            append("] ")
        }

        terminal.print(string)
        terminal.cursor.move { clearLineAfterCursor() }
        terminal.println(
            if (progress < 1)
                TextColors.brightWhite("${(progress * 100).roundToInt()}%")
            else
                TextColors.brightGreen(TextStyles.bold("done"))
        )
    }
}
