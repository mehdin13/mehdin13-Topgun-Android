package app.topgun.android.features.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.asApiError
import app.topgun.android.data.loading
import app.topgun.core.model.Country
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** How the country list is shown: grid of cards (iOS CountryListView), flat map or globe (iOS MapView). */
enum class CountryViewMode { Grid, Map, Globe }

data class CountryListState(
    val query: String = "",
    val mode: CountryViewMode = CountryViewMode.Grid,
    val initial: Load<Unit> = Load.Loading,
    val countries: List<Country> = emptyList(),
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    /** Every country (for pins), loaded lazily the first time the map or globe is shown. */
    val mapCountries: Load<List<Country>>? = null,
)

/** iOS CountryListViewModel / FeaturedViewModel: pages of 20, load more near the end, plus search by name. */
class CountryListViewModel(private val api: TopGunApi) : ViewModel() {
    private val _state = MutableStateFlow(CountryListState())
    val state: StateFlow<CountryListState> = _state.asStateFlow()

    private var page = 1
    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private var mapJob: Job? = null

    init { reload() }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            reload()
        }
    }

    fun setMode(mode: CountryViewMode) {
        _state.update { it.copy(mode = mode) }
        if (mode != CountryViewMode.Grid) loadMapCountries()
    }

    fun reload() {
        loadJob?.cancel()
        page = 1
        _state.update { it.copy(initial = Load.Loading, countries = emptyList(), hasMore = false, isLoadingMore = false) }
        val name = _state.value.query.trim().ifEmpty { null }
        loadJob = viewModelScope.launch {
            when (val r = loading { api.countries(1, PAGE_SIZE, name) }) {
                is Load.Loaded -> _state.update { it.copy(initial = Load.Loaded(Unit), countries = r.value.results, hasMore = r.value.hasNext) }
                is Load.Failed -> _state.update { it.copy(initial = r) }
                Load.Loading -> Unit
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoadingMore || !s.hasMore || s.initial !is Load.Loaded) return
        _state.update { it.copy(isLoadingMore = true) }
        val name = s.query.trim().ifEmpty { null }
        val next = page + 1
        loadJob = viewModelScope.launch {
            try {
                val r = api.countries(next, PAGE_SIZE, name)
                page = next
                _state.update { it.copy(countries = it.countries + r.results, hasMore = r.hasNext, isLoadingMore = false) }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                // Keep what is shown; the next scroll to the end tries the same page again.
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    /** Pins need coordinates of every country: fetch pages of 100 one after another (the API allows ~4 requests/second). */
    fun loadMapCountries() {
        if (_state.value.mapCountries is Load.Loaded || mapJob?.isActive == true) return
        _state.update { it.copy(mapCountries = Load.Loading) }
        mapJob = viewModelScope.launch {
            try {
                val all = mutableListOf<Country>()
                var p = 1
                while (true) {
                    val r = api.countries(p, MAP_PAGE_SIZE)
                    all += r.results
                    if (!r.hasNext) break
                    p++
                    delay(MAP_PAGE_GAP_MS)
                }
                _state.update { it.copy(mapCountries = Load.Loaded(all)) }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(mapCountries = Load.Failed(e.asApiError())) }
            }
        }
    }

    fun retryMap() {
        _state.update { it.copy(mapCountries = null) }
        loadMapCountries()
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val MAP_PAGE_SIZE = 100
        const val MAP_PAGE_GAP_MS = 300L
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
