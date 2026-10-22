package app.topgun.android.features.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.asApiError
import app.topgun.android.data.loading
import app.topgun.core.model.Blog
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BlogListState(
    val blogs: Load<List<Blog>> = Load.Loading,
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    /** Set when loading the next page failed; the loaded posts stay on screen. */
    val moreError: String? = null,
)

/** iOS `BlogViewModel` (which only fetched page 1); this one also pages through `aircrafts/blog/`. */
class BlogViewModel(private val api: TopGunApi) : ViewModel() {
    private val _state = MutableStateFlow(BlogListState())
    val state: StateFlow<BlogListState> = _state.asStateFlow()

    private var page = 1
    private var busy = false

    init { reload() }

    fun reload() {
        if (busy) return
        busy = true
        page = 1
        _state.value = BlogListState()
        viewModelScope.launch {
            val result = loading { api.blogs(1) }
            _state.value = when (result) {
                is Load.Loaded -> BlogListState(Load.Loaded(result.value.results), hasMore = result.value.hasNext)
                is Load.Failed -> BlogListState(Load.Failed(result.error))
                Load.Loading -> BlogListState()
            }
            busy = false
        }
    }

    /** Called when the end of the list is reached. One request at a time (the API throttles to about 4 per second). */
    fun loadMore() {
        val current = _state.value
        val loaded = current.blogs as? Load.Loaded ?: return
        if (busy || !current.hasMore) return
        busy = true
        _state.value = current.copy(loadingMore = true, moreError = null)
        viewModelScope.launch {
            try {
                val next = api.blogs(page + 1)
                page += 1
                val known = loaded.value.map { it.id }.toSet()
                _state.value = BlogListState(Load.Loaded(loaded.value + next.results.filterNot { it.id in known }), hasMore = next.hasNext)
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = current.copy(loadingMore = false, moreError = e.asApiError().userMessage)
            }
            busy = false
        }
    }
}
