package com.mehdin13.dreamland.core.model

import java.time.Instant
import java.time.OffsetDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/** Shared JSON configuration: snake_case keys, tolerant of new server fields and nulls. */
@OptIn(ExperimentalSerializationApi::class)
val DreamlandJson: Json = Json {
    namingStrategy = JsonNamingStrategy.SnakeCase
    ignoreUnknownKeys = true
    // null or unknown enum values fall back to the property default instead of failing the screen.
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = true
}

/** DRF datetimes with or without fractions, `Z` or an offset. */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Instant = OffsetDateTime.parse(decoder.decodeString()).toInstant()
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
}
