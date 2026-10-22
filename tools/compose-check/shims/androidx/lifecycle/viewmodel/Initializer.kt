package androidx.lifecycle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InitializerViewModelFactoryBuilder {
    val initializers = mutableMapOf<kotlin.reflect.KClass<*>, () -> ViewModel>()
    inline fun <reified VM : ViewModel> initializer(noinline create: () -> VM) { initializers[VM::class] = create }
}

inline fun <reified VM : ViewModel> InitializerViewModelFactoryBuilder.initializer(noinline create: CreationExtrasShim.() -> VM) {
    initializers[VM::class] = { CreationExtrasShim.create() }
}

object CreationExtrasShim

inline fun viewModelFactory(builder: InitializerViewModelFactoryBuilder.() -> Unit): ViewModelProvider.Factory {
    val b = InitializerViewModelFactoryBuilder().apply(builder)
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: Any?): T = b.initializers.getValue(modelClass)() as T
    }
}
