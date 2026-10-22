package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TGBrand
import coil.compose.AsyncImage

/**
 * Remote image from an API media path. Relative paths (`/media/...`) are resolved against the server origin
 * (the iOS app does `BASE_URL + path`). Shows a gray box while loading or when [path] is null.
 * SVG files (flags and icons from the backend) are decoded by Coil's SVG module (registered in `MainActivity`).
 */
@Composable
fun TGImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val media = LocalAppContainer.current.media
    val url = remember(path) { media.resolve(path) }
    Box(modifier.background(TGBrand.gray.copy(alpha = 0.3f))) {
        if (url != null) {
            AsyncImage(model = url, contentDescription = contentDescription, contentScale = contentScale, modifier = Modifier.matchParentSize())
        }
    }
}
