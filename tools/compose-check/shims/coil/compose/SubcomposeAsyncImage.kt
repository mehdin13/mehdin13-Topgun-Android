package coil.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

/** Shim of Coil 2.7 `SubcomposeAsyncImage`: the scope is a BoxScope, state classes carry the painter like Coil's. */
interface SubcomposeAsyncImageScope : BoxScope

object AsyncImagePainter {
    sealed interface State {
        data class Loading(val painter: Painter?) : State
        data class Success(val painter: Painter, val result: Any?) : State
        data class Error(val painter: Painter?, val result: Any?) : State
    }
}

@Composable
fun SubcomposeAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    loading: @Composable (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Loading) -> Unit)? = null,
    success: @Composable (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Success) -> Unit)? = null,
    error: @Composable (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
) {
    Box(modifier) {
        val scope = object : SubcomposeAsyncImageScope, BoxScope by this {}
        loading?.invoke(scope, AsyncImagePainter.State.Loading(null))
        error?.invoke(scope, AsyncImagePainter.State.Error(null, null))
    }
}
