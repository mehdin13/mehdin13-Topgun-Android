package app.topgun.android.features.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.loading
import app.topgun.core.model.Country
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** iOS CountryDetailViewModel: `GET aircrafts/country/{id}/`. */
class CountryDetailViewModel(private val api: TopGunApi, private val id: Int) : ViewModel() {
    private val _state = MutableStateFlow<Load<Country>>(Load.Loading)
    val state: StateFlow<Load<Country>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = Load.Loading
        viewModelScope.launch { _state.value = loading { api.country(id) } }
    }
}
