package com.mehdin13.dreamland.core

import com.mehdin13.dreamland.core.model.DreamlandJson
import com.mehdin13.dreamland.core.model.GenerationQuotaDto
import com.mehdin13.dreamland.core.model.PublicStoryDto
import com.mehdin13.dreamland.core.model.StoryDto
import com.mehdin13.dreamland.core.model.StoryStatus
import com.mehdin13.dreamland.core.model.storyListSerializer
import java.time.Instant
import kotlinx.serialization.descriptors.elementNames
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
class DtoDecodingTest {
    private fun story(extra: String = "", status: String = "completed", created: String = "2026-10-11T10:00:00Z") = """
        {"id":7,"title":"The Lighthouse Fox","status":"$status","content":"Once upon a time",
         "created_at":"$created","updated_at":"2026-10-11T10:05:00.123456+02:00"$extra}
    """.trimIndent()

    @Test fun decodesSnakeCaseAndMissingFieldsWithDefaults() {
        val dto = DreamlandJson.decodeFromString(StoryDto.serializer(), story(""","favorite_count":3,"is_favorited_by_me":true"""))
        assertEquals(7, dto.id)
        assertEquals(3, dto.favoriteCount)
        assertTrue(dto.isFavoritedByMe)
        assertEquals(StoryStatus.COMPLETED, dto.status)
        assertEquals(emptyList(), dto.interests)
    }

    @Test fun nullMediaStaysNull() {
        val dto = DreamlandJson.decodeFromString(
            StoryDto.serializer(),
            story(""","cover_image_url":null,"audio_url":null,"pdf_url":null,"source_painting_url":null"""),
        )
        assertNull(dto.coverImageUrl)
        assertNull(dto.audioUrl)
        assertNull(dto.pdfUrl)
        assertNull(dto.sourcePaintingUrl)
    }

    @Test fun unknownEnumValueBecomesUnknown() {
        assertEquals(StoryStatus.UNKNOWN, DreamlandJson.decodeFromString(StoryDto.serializer(), story(status = "queued")).status)
        assertEquals(StoryStatus.GENERATING, DreamlandJson.decodeFromString(StoryDto.serializer(), story(status = "generating")).status)
    }

    @Test fun parsesDatesWithAndWithoutFractionsAndOffsets() {
        val dto = DreamlandJson.decodeFromString(StoryDto.serializer(), story())
        assertEquals(Instant.parse("2026-10-11T10:00:00Z"), dto.createdAt)
        assertEquals(Instant.parse("2026-10-11T08:05:00.123456Z"), dto.updatedAt)
    }

    @Test fun ignoresUnknownKeys() {
        DreamlandJson.decodeFromString(StoryDto.serializer(), story(""","brand_new_field":{"a":1}"""))
    }

    @Test fun decodesQuota() {
        val q = DreamlandJson.decodeFromString(
            GenerationQuotaDto.serializer(),
            """{"image_story":{"daily_limit":5,"used_today":2,"remaining_today":3}}""",
        )
        assertEquals(3, q.imageStory.remainingToday)
    }

    @Test fun storyListAcceptsArrayOrDrfPage() {
        val row = """{"id":1,"title":"A","created_at":"2026-10-11T10:00:00Z"}"""
        val s = storyListSerializer(PublicStoryDto.serializer())
        assertEquals(1, DreamlandJson.decodeFromString(s, "[$row]").size)
        assertEquals(1, DreamlandJson.decodeFromString(s, """{"count":1,"next":null,"results":[$row]}""").size)
    }

    @Test fun publicRowsCarryNoPaintingField() {
        val names = PublicStoryDto.serializer().descriptor.elementNames.toSet()
        assertTrue("source_painting_url" !in names && "sourcePaintingUrl" !in names)
        assertTrue("content" !in names)
    }
}
