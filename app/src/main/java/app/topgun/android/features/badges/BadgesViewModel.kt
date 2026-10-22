package app.topgun.android.features.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.Load
import app.topgun.android.data.loading
import app.topgun.core.model.Badge
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Badges come with `users/user_info/` (`badges[].is_have`). The iOS screen still shows a hard-coded list (TODO there). */
class BadgesViewModel(private val api: TopGunApi, private val isSignedIn: () -> Boolean) : ViewModel() {
    private val _state = MutableStateFlow<Load<List<Badge>>>(Load.Loading)
    val state: StateFlow<Load<List<Badge>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        if (!isSignedIn()) { _state.value = Load.Loaded(emptyList()); return }
        _state.value = Load.Loading
        viewModelScope.launch { _state.value = loading { api.userInfo().badges } }
    }
}
