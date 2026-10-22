package app.topgun.android.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.asApiError
import app.topgun.core.model.Aircraft
import app.topgun.core.model.AircraftFilter
import app.topgun.core.model.Page
import app.topgun.core.network.ApiError
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** What the aircraft list currently shows. Only one source is active at a time. */
sealed interface HomeSource {
    /** `aircrafts/aircraft/` (iOS `fetchInitialAircrafts`). */
    data object All : HomeSource
    /** Category chip: filter endpoint with only `category` (iOS `filterByCategory`). */
    data class Category(val name: String) : HomeSource
    /** Search on submit (iOS `performSearch`). */
    data class Search(val query: String) : HomeSource
    /** Filter sheet result (iOS `filterAircrafts`). */
    data class Filter(val filter: AircraftFilter) : HomeSource
}

/** Everything the Home list needs to draw (iOS `HomeViewModel` published properties). */
data class HomeState(
    val items: List<Aircraft> = emptyList(),
    val source: HomeSource = HomeSource.All,
    /** First page of the current source is loading (iOS `isLoading`). */
    val isLoading: Boolean = true,
    /** "Load More" is loading (iOS `isPaginationLoading`). */
    val isPaginating: Boolean = false,
    /** The backend reported a next page (`next != null`). */
    val hasMore: Boolean = false,
    val error: ApiError? = null,
    /** Set when a Load More failed; the list stays and the screen shows a snackbar. */
    val paginationError: ApiError? = null,
) {
    val selectedCategory: String? get() = (source as? HomeSource.Category)?.name
}

/**
 * Loads and pages the Home aircraft list for the active [HomeSource]. Requests run one at a time: starting a
 * new source cancels the previous request, and Load More is ignored while another request is running, because
 * the backend throttles to about 4 requests per second.
 */
class HomeViewModel(private val api: TopGunApi, private val pageSize: Int = PAGE_SIZE) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var job: Job? = null
    private var page = 1

    init { showAll() }

    /** Back to the plain list (first load, pull-to-refresh, cleared category, empty search). */
    fun showAll() = load(HomeSource.All)

    fun selectCategory(name: String) {
        // Tapping the selected category again clears it (iOS CategoryItem).
        if (_state.value.selectedCategory == name) showAll() else load(HomeSource.Category(name))
    }

    fun search(query: String) {
        val q = query.trim()
        if (q.isEmpty()) showAll() else load(HomeSource.Search(q))
    }

    fun applyFilter(filter: AircraftFilter) {
        if (filter.isEmpty) showAll() else load(HomeSource.Filter(filter))
    }

    /** Pull-to-refresh (iOS `refreshContent`): resets to the plain list and suspends until it has loaded. */
    suspend fun refresh() {
        showAll()
        job?.join()
    }

    /** Reloads the current source from page 1 (the "Try again" button). */
    fun retry() = load(_state.value.source)

    private fun load(source: HomeSource) {
        job?.cancel()
        page = 1
        _state.value = HomeState(items = emptyList(), source = source, isLoading = true)
        job = viewModelScope.launch {
            try {
                val result = fetch(source, 1)
                _state.update { it.copy(items = result.results, hasMore = result.hasNext, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(isLoading = false, hasMore = false, error = e.asApiError()) }
            }
        }
    }

    /** Appends the next page of the current source. No-op while a request is running. */
    fun loadMore() {
        val s = _state.value
        if (s.isLoading || s.isPaginating || !s.hasMore) return
        val next = page + 1
        _state.update { it.copy(isPaginating = true, paginationError = null) }
        job = viewModelScope.launch {
            try {
                val result = fetch(s.source, next)
                page = next
                _state.update { cur ->
                    // Never show the same aircraft twice if the server list shifted between pages.
                    val known = cur.items.mapTo(HashSet()) { it.id }
                    cur.copy(items = cur.items + result.results.filter { it.id !in known }, hasMore = result.hasNext, isPaginating = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(isPaginating = false, paginationError = e.asApiError()) }
            }
        }
    }

    fun clearPaginationError() = _state.update { it.copy(paginationError = null) }

    private suspend fun fetch(source: HomeSource, page: Int): Page<Aircraft> = when (source) {
        HomeSource.All -> api.aircraft(page, pageSize)
        is HomeSource.Category -> api.aircraftByCategory(source.name, page, pageSize)
        is HomeSource.Search -> api.searchAircraft(source.query, page, pageSize)
        is HomeSource.Filter -> api.filterAircraft(source.filter, page, pageSize)
    }

    /**
     * Toggles the favorite on the server and updates the row. Returns the new state (true = added), or throws
     * [ApiError]; the caller must check sign-in first (the endpoint needs a token).
     */
    suspend fun toggleFavorite(aircraftId: Int): Boolean {
        val nowFavorite = api.toggleFavorite(aircraftId)
        _state.update { s -> s.copy(items = s.items.map { if (it.id == aircraftId) it.copy(isFavorite = nowFavorite) else it }) }
        return nowFavorite
    }

    companion object {
        /** iOS `pageSize = 10`. */
        const val PAGE_SIZE = 10
    }
}
