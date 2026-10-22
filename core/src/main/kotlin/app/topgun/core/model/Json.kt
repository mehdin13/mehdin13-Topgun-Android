package app.topgun.core.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonPrimitive

/** Shared JSON configuration: snake_case keys, tolerant of new server fields and nulls. */
@OptIn(ExperimentalSerializationApi::class)
val TopGunJson: Json = Json {
    namingStrategy = JsonNamingStrategy.SnakeCase
    ignoreUnknownKeys = true
    // null or unknown enum values fall back to the property default instead of failing a screen.
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = true
}

/**
 * Reads a JSON string, number or boolean as a String. DRF sends DecimalFields as strings but the iOS models
 * treat several of these fields as text, and a backend change from string to number must not break a screen.
 */
object FlexString : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("FlexString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        return (element as? JsonPrimitive)?.content ?: element.toString()
    }

    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}
