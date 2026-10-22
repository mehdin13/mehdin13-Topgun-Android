package app.topgun.android.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.asApiError
import app.topgun.android.data.loading
import app.topgun.core.model.Aircraft
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** iOS `FavoriteViewModel`: loads `users/favorites/` and removes with the toggle endpoint. */
class FavoritesViewModel(private val api: TopGunApi) : ViewModel() {
    private val _state = MutableStateFlow<Load<List<Aircraft>>>(Load.Loading)
    val state: StateFlow<Load<List<Aircraft>>> = _state.asStateFlow()

    /** Shown above the list when a removal failed. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val removing = mutableSetOf<Int>()

    /**
     * Fetches the list. A refresh keeps the current list on screen. The backend reports `is_favorite=false` for every
     * item of this list (docs/BACKEND_GAPS.md #7), so everything returned is treated as a favorite.
     */
    fun load() {
        viewModelScope.launch {
            if (_state.value is Load.Failed) _state.value = Load.Loading
            val result = loading { api.favorites().aircrafts.map { it.copy(isFavorite = true) } }
            // A failed refresh must not wipe a list that is already showing.
            if (result is Load.Failed && _state.value is Load.Loaded) {
                _message.value = result.error.userMessage
            } else {
                _state.value = result
            }
        }
    }

    /** Removes [aircraft] (toggle). The row disappears at once; if the request fails the list is reloaded. */
    fun remove(aircraft: Aircraft) {
        if (!removing.add(aircraft.id)) return
        val current = _state.value as? Load.Loaded ?: run { removing.remove(aircraft.id); return }
        _state.value = Load.Loaded(current.value.filterNot { it.id == aircraft.id })
        viewModelScope.launch {
            try {
                val added = api.toggleFavorite(aircraft.id)
                // `true` would mean the server had it as not-favorite, so the toggle re-added it: undo to keep the list honest.
                if (added) api.toggleFavorite(aircraft.id)
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _message.value = e.asApiError().userMessage
                removing.remove(aircraft.id)
                load()
                return@launch
            }
            removing.remove(aircraft.id)
        }
    }

    fun dismissMessage() { _message.value = null }
}
