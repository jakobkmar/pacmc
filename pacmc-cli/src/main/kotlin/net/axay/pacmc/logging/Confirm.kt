package net.axay.pacmc.logging

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.terminal.Terminal

fun Terminal.awaitConfirmation(): Boolean {
    print(" (${TextColors.brightGreen("y")}es / ${TextColors.brightRed("n")}o) ")
    var sure: Boolean? = null
    while (sure == null) {
        sure = when (readLine()) {
            "y", "yes" -> true
            "n", "no", null -> false
            else -> {
                print("Please type in ${TextColors.brightGreen("y")}es ${bold("or")} ${TextColors.brightRed("n")}o: ")
                null
            }
        }
    }
    return sure
}

fun Terminal.awaitContinueAnyways(): Boolean {
    print("Do you want to continue anyways?")
    return awaitConfirmation()
}
