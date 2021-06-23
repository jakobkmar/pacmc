package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

object Remove : CliktCommand(
    "Removes a minecraft mod"
) {
    private val archiveName by option("-a", "--archive").default(".minecraft")

    override fun run() {
        
    }
}
