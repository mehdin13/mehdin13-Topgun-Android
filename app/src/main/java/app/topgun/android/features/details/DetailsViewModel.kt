package app.topgun.android.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.asApiError
import app.topgun.android.data.loading
import app.topgun.core.model.AircraftDetail
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** What the detail screen shows. [detail] is the server copy; the favorite/like/rating fields are the live values. */
data class DetailsState(
    val detail: Load<AircraftDetail> = Load.Loading,
    val isFavorite: Boolean = false,
    val isLiked: Boolean = false,
    val likesCount: Int = 0,
    val averageRating: Double? = null,
    /** Stars the user just tapped (iOS `userRating`); 0 until they rate. */
    val userRating: Int = 0,
    val isRating: Boolean = false,
    val busy: Boolean = false,
    /** Short message shown in a toast (iOS Toast "Added To Favorite"), consumed by [DetailsViewModel.toastShown]. */
    val toast: DetailToast? = null,
    val error: String? = null,
)

/** Which toast to show; the composable turns it into localized text. */
enum class DetailToast { FavoriteAdded, FavoriteRemoved }

/** iOS `DetailsViewModel` plus the rate and like requests that lived in the view. */
class DetailsViewModel(private val api: TopGunApi, private val aircraftId: Int) : ViewModel() {
    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = DetailsState()
        viewModelScope.launch {
            val result = loading { api.aircraftDetail(aircraftId) }
            _state.value = when (result) {
                is Load.Loaded -> DetailsState(
                    detail = result,
                    isFavorite = result.value.isFavorite,
                    likesCount = result.value.likesCount,
                    averageRating = result.value.averageRating,
                )
                else -> DetailsState(detail = result)
            }
        }
    }

    /** The caller has checked sign-in. The backend toggles and says whether the aircraft is now a favorite. */
    fun toggleFavorite() {
        if (_state.value.busy) return
        _state.value = _state.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                val added = api.toggleFavorite(aircraftId)
                _state.value = _state.value.copy(
                    isFavorite = added, busy = false,
                    toast = if (added) DetailToast.FavoriteAdded else DetailToast.FavoriteRemoved,
                )
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = _state.value.copy(busy = false, error = e.asApiError().userMessage)
            }
        }
    }

    fun toggleLike() {
        if (_state.value.busy) return
        _state.value = _state.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                val reply = api.toggleLike(aircraftId)
                val count = _state.value.likesCount + if (reply.isLiked) 1 else -1
                _state.value = _state.value.copy(isLiked = reply.isLiked, likesCount = count.coerceAtLeast(0), busy = false)
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = _state.value.copy(busy = false, error = e.asApiError().userMessage)
            }
        }
    }

    fun rate(stars: Int) {
        if (_state.value.isRating) return
        _state.value = _state.value.copy(userRating = stars, isRating = true, error = null)
        viewModelScope.launch {
            try {
                val reply = api.rate(aircraftId, stars)
                _state.value = _state.value.copy(isRating = false, averageRating = reply.averageRating ?: _state.value.averageRating)
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                // iOS resets the stars when the request fails.
                _state.value = _state.value.copy(isRating = false, userRating = 0, error = e.asApiError().userMessage)
            }
        }
    }

    fun toastShown() { _state.value = _state.value.copy(toast = null) }
    fun errorShown() { _state.value = _state.value.copy(error = null) }
}
