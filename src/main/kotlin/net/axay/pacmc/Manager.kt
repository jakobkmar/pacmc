package net.axay.pacmc

import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import net.axay.pacmc.commands.Pacmc

fun main(args: Array<String>) = Pacmc.main(args)

val terminal = Terminal()

val ktorClient by lazy {
    HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Values.json)
        }
    }
}
