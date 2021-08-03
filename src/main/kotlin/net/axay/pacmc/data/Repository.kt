package net.axay.pacmc.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Repository.RepoSerializer::class)
enum class Repository(val stringName: String) {
    MODRINTH("modrinth"),
    CURSEFORGE("curseforge");

    override fun toString() = stringName

    object RepoSerializer : KSerializer<Repository> {
        override val descriptor = PrimitiveSerialDescriptor("Repository", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Repository {
            return valueOf(decoder.decodeString().uppercase())
        }

        override fun serialize(encoder: Encoder, value: Repository) {
            encoder.encodeString(value.name.lowercase())
        }
    }
}
