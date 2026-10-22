package com.mehdin13.dreamland.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mehdin13.dreamland.DreamlandApp
import com.mehdin13.dreamland.core.network.ApiError

@Composable
fun rememberContainer(): AppContainer = (LocalContext.current.applicationContext as DreamlandApp).container

/** Creates (or reuses) a ViewModel built from the app container. [key] separates instances, such as one per story id. */
@Composable
inline fun <reified VM : ViewModel> containerViewModel(key: String? = null, crossinline create: (AppContainer) -> VM): VM {
    val container = rememberContainer()
    return viewModel(key = key, factory = viewModelFactory { initializer { create(container) } })
}

/** What a screen shows while a request is loading, done, or failed. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Loaded<T>(val value: T) : Load<T>
    data class Failed(val error: ApiError) : Load<Nothing>
}

/** Turns any thrown error into an [ApiError] so views only ever see friendly messages. */
fun Throwable.asApiError(): ApiError = when (this) {
    is ApiError -> this
    is java.io.IOException -> ApiError.Network
    else -> ApiError.UnexpectedStatus(0)
}
