package net.axay.pacmc

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import net.axay.pacmc.commands.Pacmc

fun main(args: Array<String>) = try {
    Pacmc.main(args)
} catch (exception: Exception) {
    exception.printStackTrace()
    terminal.println(
        TextColors.brightRed("An error occured,") +
                TextColors.yellow(" please open an issue with the above stack trace at ") +
                TextStyles.underline("https://github.com/jakobkmar/pacmc/issues/new")
    )
}

val terminal = Terminal()

val ktorClient by lazy {
    HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Values.json)
        }
    }
}
