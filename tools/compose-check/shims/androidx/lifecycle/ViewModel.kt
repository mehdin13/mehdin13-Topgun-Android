package androidx.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

open class ViewModel {
    internal val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    protected open fun onCleared() {}
}

val ViewModel.viewModelScope: CoroutineScope get() = scope

interface ViewModelProvider {
    interface Factory {
        fun <T : ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: Any? = null): T
    }
}
