package com.mehdin13.dreamland.core.network

import com.mehdin13.dreamland.core.model.DreamlandJson
import com.mehdin13.dreamland.core.model.FavoriteResponseDto
import com.mehdin13.dreamland.core.model.GenerationQuotaDto
import com.mehdin13.dreamland.core.model.PublicStoryDto
import com.mehdin13.dreamland.core.model.StoryDto
import com.mehdin13.dreamland.core.model.StorySummaryDto
import com.mehdin13.dreamland.core.model.UserDto
import com.mehdin13.dreamland.core.model.storyListSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Sort options for the public story list. [value] is the backend's `sort` query value. */
enum class FeedSort(val value: String, val label: String) {
    RECENT("recent", "Recent"),
    RATING("rating", "Top rated"),
    POPULAR("popular", "Popular"),
}

/**
 * Typed calls against the story endpoints. Paths follow docs/BACKEND_GAPS.md in dreamland-ios:
 * the Dreamland backend is the source of truth and is not in this repo, so unconfirmed ones are marked.
 */
class StoryApi(private val client: ApiClientContract) {
    private val publicList = storyListSerializer(PublicStoryDto.serializer())

    suspend fun myStories(): List<StorySummaryDto> =
        client.send(ApiEndpoint.get("stories/my_stories/"), storyListSerializer(StorySummaryDto.serializer()))

    suspend fun story(id: Int): StoryDto = client.send(ApiEndpoint.get("stories/$id/"), StoryDto.serializer())

    suspend fun deleteStory(id: Int) = client.send(ApiEndpoint.delete("stories/$id/"))

    /** Public completed stories. `category` filters `genre` (valid values unconfirmed, BACKEND_GAPS #19). */
    suspend fun publicStories(sort: FeedSort = FeedSort.RECENT, category: String? = null, search: String? = null): List<PublicStoryDto> {
        val query = buildList {
            add("sort" to sort.value)
            category?.takeIf { it.isNotBlank() }?.let { add("category" to it) }
            search?.takeIf { it.isNotBlank() }?.let { add("search" to it) }
        }
        return client.send(ApiEndpoint.get("stories/public/", query, requiresAuth = false), publicList)
    }

    /** The backend path differs from the spec (BACKEND_GAPS #6): `stories/public/most-favorited/`. */
    suspend fun mostFavorited(): List<PublicStoryDto> =
        client.send(ApiEndpoint.get("stories/public/most-favorited/", requiresAuth = false), publicList)

    suspend fun sharedStory(token: String): StoryDto =
        client.send(ApiEndpoint.get("stories/shared/$token/", requiresAuth = false), StoryDto.serializer())

    suspend fun myCollection(): List<PublicStoryDto> = client.send(ApiEndpoint.get("stories/my_collection/"), publicList)

    /** Not on the backend yet (BACKEND_GAPS #43); expect 404 until it ships. */
    suspend fun myFavorites(): List<PublicStoryDto> = client.send(ApiEndpoint.get("stories/my_favorites/"), publicList)

    suspend fun quota(): GenerationQuotaDto = client.send(ApiEndpoint.get("stories/generation-quota/"), GenerationQuotaDto.serializer())

    suspend fun favorite(id: Int): FavoriteResponseDto =
        client.send(ApiEndpoint.post("stories/$id/favorite/"), FavoriteResponseDto.serializer())

    suspend fun unfavorite(id: Int): FavoriteResponseDto =
        client.send(ApiEndpoint.delete("stories/$id/favorite/"), FavoriteResponseDto.serializer())

    suspend fun save(id: Int) = client.send(ApiEndpoint.post("stories/$id/save/"))

    /** The server answers 204 (with a JSON body, BACKEND_GAPS #47); the body is ignored. */
    suspend fun unsave(id: Int) = client.send(ApiEndpoint.delete("stories/$id/save/"))

    suspend fun rate(id: Int, score: Int) {
        require(score in 1..5) { "score must be 1..5" }
        val json = buildJsonObject { put("score", score) }
        client.send(ApiEndpoint.postJson("stories/$id/rate/", DreamlandJson.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), json)))
    }

    suspend fun share(id: Int): StoryDto = client.send(ApiEndpoint.post("stories/$id/share/"), StoryDto.serializer())

    suspend fun unshare(id: Int): StoryDto = client.send(ApiEndpoint.post("stories/$id/unshare/"), StoryDto.serializer())

    /** `GET /users/me/`. Not mounted on the backend yet (BACKEND_GAPS #1). */
    suspend fun me(): UserDto = client.send(ApiEndpoint.get("users/me/"), UserDto.serializer())
}
