package app.topgun.android.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.asApiError
import app.topgun.android.data.loading
import app.topgun.core.model.Comment
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/** A row of the comments list (iOS `CommentViewData`). */
data class CommentRow(val id: Int, val username: String, val text: String, val timeAgo: String)

data class CommentsState(
    val comments: Load<List<CommentRow>> = Load.Loading,
    val isSending: Boolean = false,
    val sendError: String? = null,
)

/** iOS `CommentsViewModel`. [selfUserId] (0 when unknown) turns "User 7" into "You" for the signed-in user's own comments. */
class CommentsViewModel(private val api: TopGunApi, private val aircraftId: Int, private val selfUserId: Int) : ViewModel() {
    private val _state = MutableStateFlow(CommentsState())
    val state: StateFlow<CommentsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = _state.value.copy(comments = Load.Loading)
        viewModelScope.launch { fetch() }
    }

    private suspend fun fetch() {
        val result = loading { api.comments(aircraftId).results.map(::toRow) }
        _state.value = _state.value.copy(comments = result)
    }

    /** Posts, then reloads the list (the server only answers "Created New Comment"). [onDone] gets true on success. */
    fun post(body: String, onDone: (Boolean) -> Unit) {
        val text = body.trim()
        if (text.isEmpty() || _state.value.isSending) return
        _state.value = _state.value.copy(isSending = true, sendError = null)
        viewModelScope.launch {
            try {
                api.postComment(aircraftId, text)
                _state.value = _state.value.copy(isSending = false)
                fetch()
                onDone(true)
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = _state.value.copy(isSending = false, sendError = "Failed to post comment: " + e.asApiError().userMessage)
                onDone(false)
            }
        }
    }

    private fun toRow(c: Comment) = CommentRow(
        id = c.id,
        username = if (selfUserId != 0 && c.user == selfUserId) "You" else "User ${c.user}",
        text = c.body,
        timeAgo = timeAgo(c.created),
    )

    companion object {
        /** iOS `timeAgo(from:)`: "Just now", "5m ago", "3h ago", "2d ago"; empty when the date cannot be read. */
        fun timeAgo(iso: String, now: Instant = Instant.now()): String {
            val then = runCatching { Instant.parse(iso) }.getOrNull()
                ?: runCatching { java.time.OffsetDateTime.parse(iso).toInstant() }.getOrNull()
                ?: return ""
            val seconds = (now.epochSecond - then.epochSecond).coerceAtLeast(0)
            if (seconds < 60) return "Just now"
            val minutes = seconds / 60
            if (minutes < 60) return "${minutes}m ago"
            val hours = minutes / 60
            if (hours < 24) return "${hours}h ago"
            return "${hours / 24}d ago"
        }
    }
}
