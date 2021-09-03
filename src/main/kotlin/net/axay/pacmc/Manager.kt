package net.axay.pacmc

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import net.axay.pacmc.commands.Pacmc
import net.axay.pacmc.logging.Printer

fun main(args: Array<String>) = try {
    Pacmc.main(args)
} catch (exception: Exception) {
    exception.printStackTrace()
    terminal.println(TextColors.brightRed("An error occured, ") + Printer.issueLink("the above stack trace"))
}

val terminal = Terminal()

val ktorClient by lazy {
    HttpClient(CIO) {
        expectSuccess = false

        install(JsonFeature) {
            serializer = KotlinxSerializer(Values.json)
        }
    }
}
