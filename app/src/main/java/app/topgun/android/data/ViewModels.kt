package app.topgun.android.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

val LocalAppContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer not provided") }

/**
 * Creates (or reuses) a ViewModel built from the app container. [key] separates instances,
 * for example one per aircraft id: `containerViewModel("details-$id") { DetailsViewModel(it.api, id) }`.
 */
@Composable
inline fun <reified VM : ViewModel> containerViewModel(key: String? = null, crossinline create: (AppContainer) -> VM): VM {
    val container = LocalAppContainer.current
    return viewModel(key = key, factory = viewModelFactory { initializer { create(container) } })
}
