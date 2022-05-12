package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.terminal.Terminal

fun <T> Terminal.choose(
    message: String,
    entries: List<Pair<T, String>>,
): T? {
    entries.forEachIndexed { index, (_, string) ->
        print("${index + 1}) ")
        println(string)
    }
    while (true) {
        print("$message (provide the number): ")
        val input = readlnOrNull() ?: return null

        if (input.isEmpty()) {
            warning("Please enter a number")
            continue
        }

        val index = input.toIntOrNull()?.minus(1)
        if (index == null) {
            warning("'$input' is not a valid number")
            continue
        }

        if (index !in entries.indices) {
            warning("'$input' is not a valid choice")
            continue
        }

        return entries.getOrNull(index)?.first
    }
}
