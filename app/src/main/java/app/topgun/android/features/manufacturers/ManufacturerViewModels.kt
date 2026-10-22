package app.topgun.android.features.manufacturers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.loading
import app.topgun.core.model.Manufacturer
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** iOS ManufacturerListViewModel: `custom-manufacturer-list/?name=Top manufacturers`, first list only. */
class ManufacturerListViewModel(private val api: TopGunApi) : ViewModel() {
    private val _state = MutableStateFlow<Load<List<Manufacturer>>>(Load.Loading)
    val state: StateFlow<Load<List<Manufacturer>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = Load.Loading
        viewModelScope.launch {
            _state.value = loading { api.customManufacturerList("Top manufacturers").firstOrNull()?.manufacturerList.orEmpty() }
        }
    }
}

/** iOS ManufacturerViewModel: `GET aircrafts/manufacturer/{id}/`. */
class ManufacturerDetailViewModel(private val api: TopGunApi, private val id: Int) : ViewModel() {
    private val _state = MutableStateFlow<Load<Manufacturer>>(Load.Loading)
    val state: StateFlow<Load<Manufacturer>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = Load.Loading
        viewModelScope.launch { _state.value = loading { api.manufacturer(id) } }
    }
}
