package com.mehdin13.dreamland.core.model

import java.time.Instant
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

/** Story generation state. Unknown server values decode to [UNKNOWN] (via coerceInputValues). */
@Serializable
enum class StoryStatus {
    @SerialName("generating") GENERATING,
    @SerialName("completed") COMPLETED,
    @SerialName("failed") FAILED,
    @SerialName("unknown") UNKNOWN,
}

/** One row from `GET /stories/` and `GET /stories/my_stories/` (StoryListSerializer). */
@Serializable
data class StorySummaryDto(
    val id: Int,
    val childName: String? = null,
    val ageRange: String? = null,
    val theme: String? = null,
    val interests: List<String> = emptyList(),
    val title: String = "",
    val status: StoryStatus = StoryStatus.UNKNOWN,
    val coinsSpent: Int = 0,
    val isPublic: Boolean = false,
    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
)

/**
 * Full story from `GET /stories/{id}/` and the create/update responses (StorySerializer).
 * Media fields are raw server strings, often relative and often null while generation runs.
 * Resolve them with `MediaUrlResolver`; never assume they are present or absolute.
 */
@Serializable
data class StoryDto(
    val id: Int,
    val userEmail: String? = null,
    val childName: String? = null,
    val childGender: String = "",
    val ageRange: String? = null,
    val timeLength: String = "",
    val userContent: String? = null,
    val userAudioInputUrl: String? = null,
    val mainCharacter: String? = null,
    val interests: List<String> = emptyList(),
    val style: String = "",
    val theme: String? = null,
    val genre: String? = null,
    val moralLesson: String? = null,
    val mythology: String = "",
    val place: String? = null,
    val language: String = "en",
    val voiceType: String = "",
    val generateAudio: Boolean = false,
    val generatePdf: Boolean = false,
    val title: String = "",
    val content: String = "",
    val pdfUrl: String? = null,
    val audioUrl: String? = null,
    val coverImageUrl: String? = null,
    /** The child's original painting. Owner-only: never show it in Discover or shared views. */
    val sourcePaintingUrl: String? = null,
    val favoriteCount: Int = 0,
    val isFavoritedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val isPublic: Boolean = false,
    val shareUrl: String? = null,
    val sharedCount: Int = 0,
    val viewCount: Int = 0,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val status: StoryStatus = StoryStatus.UNKNOWN,
    val coinsSpent: Int = 0,
    val generationTime: Double? = null,
    val errorMessage: String = "",
    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
    @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
)

/** One row from the public lists and `my_collection` (PublicStorySerializer). Carries no painting and no story text. */
@Serializable
data class PublicStoryDto(
    val id: Int,
    val title: String = "",
    val genre: String? = null,
    val interests: List<String> = emptyList(),
    val ageRange: String? = null,
    val theme: String? = null,
    val coverImageUrl: String? = null,
    val authorName: String? = null,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val viewCount: Int = 0,
    val sharedCount: Int = 0,
    val isSavedByMe: Boolean = false,
    val isFavoritedByMe: Boolean = false,
    val favoriteCount: Int = 0,
    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
)

/** `GET /stories/generation-quota/` */
@Serializable
data class GenerationQuotaDto(val imageStory: ImageStoryQuota) {
    @Serializable
    data class ImageStoryQuota(val dailyLimit: Int, val usedToday: Int, val remainingToday: Int)
}

/** `POST/DELETE /stories/{id}/favorite/` */
@Serializable
data class FavoriteResponseDto(val favorited: Boolean, val storyId: Int, val favoriteCount: Int)

/** `GET /users/me/` (UserSerializer). The route is not mounted on the backend yet (BACKEND_GAPS #1). */
@Serializable
data class UserDto(
    val id: Int,
    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val provider: String = "",
    val providerId: String? = null,
    val avatar: String? = null,
    val dateOfBirth: String? = null,
    val coinBalance: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalCoinsSpent: Int = 0,
    @Serializable(with = InstantSerializer::class) val createdAt: Instant? = null,
    val lastLoginRewardDate: String? = null,
)

/**
 * A story list that is either a plain array (today) or a DRF page with `results` (if pagination is switched on).
 * Use [storyListSerializer] to decode it into a plain list.
 */
fun <T> storyListSerializer(item: KSerializer<T>): DeserializationStrategy<List<T>> = object : DeserializationStrategy<List<T>> {
    private val list = ListSerializer(item)
    override val descriptor: SerialDescriptor = list.descriptor

    override fun deserialize(decoder: Decoder): List<T> {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        val array = when (element) {
            is JsonArray -> element
            is JsonObject -> element["results"] as? JsonArray
                ?: throw kotlinx.serialization.SerializationException("Expected an array or a page with results")
            else -> throw kotlinx.serialization.SerializationException("Expected an array or a page with results")
        }
        return jsonDecoder.json.decodeFromJsonElement(list, array)
    }
}
