package app.topgun.android.features.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.loading
import app.topgun.core.model.CustomAircraftList
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** iOS CustomAircraftListViewModel: `custom-aircraft-list/?name=...`, the first list of the answer (or an empty list). */
class CustomAircraftListViewModel(private val api: TopGunApi, private val listName: String) : ViewModel() {
    private val _state = MutableStateFlow<Load<CustomAircraftList>>(Load.Loading)
    val state: StateFlow<Load<CustomAircraftList>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = Load.Loading
        viewModelScope.launch {
            _state.value = loading { api.customAircraftList(listName).firstOrNull() ?: CustomAircraftList(listName) }
        }
    }
}
