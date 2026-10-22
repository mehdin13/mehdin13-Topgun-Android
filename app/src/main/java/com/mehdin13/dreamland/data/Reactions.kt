package com.mehdin13.dreamland.data

import com.mehdin13.dreamland.core.model.PublicStoryDto
import com.mehdin13.dreamland.core.network.ApiError
import com.mehdin13.dreamland.core.network.StoryApi
import kotlin.coroutines.cancellation.CancellationException

/**
 * One favorite behavior for every list. Flips the heart at once, then takes the count from the server's reply.
 * On failure it restores the previous state and returns the error. [update] is called with each state in turn.
 * The caller disables the control while this runs (see FavoriteState.isUpdating).
 */
suspend fun StoryApi.toggleFavorite(story: PublicStoryDto, update: (PublicStoryDto) -> Unit): ApiError? {
    val wasFavorited = story.isFavoritedByMe
    val optimistic = story.copy(
        isFavoritedByMe = !wasFavorited,
        favoriteCount = (story.favoriteCount + if (wasFavorited) -1 else 1).coerceAtLeast(0),
    )
    update(optimistic)
    return try {
        val reply = if (wasFavorited) unfavorite(story.id) else favorite(story.id)
        update(optimistic.copy(isFavoritedByMe = reply.favorited, favoriteCount = reply.favoriteCount))
        null
    } catch (e: CancellationException) {
        update(story)
        throw e
    } catch (e: Throwable) {
        update(story)
        e.asApiError()
    }
}

fun List<PublicStoryDto>.replacing(updated: PublicStoryDto): List<PublicStoryDto> =
    map { if (it.id == updated.id) updated else it }
