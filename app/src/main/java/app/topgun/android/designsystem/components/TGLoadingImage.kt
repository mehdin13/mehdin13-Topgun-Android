package app.topgun.android.designsystem.components

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGImageState
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Gallery image with explicit states, built on Coil's [SubcomposeAsyncImage] and resolved like [TGImage]:
 * a centred spinner on the card background until the image has fully loaded, and a tap-to-retry error state
 * (broken image icon with "Tap to retry") when the request fails. Use it for galleries, full-screen viewers,
 * thumbnails and sketches; plain [TGImage] stays for list cards where a gray box is enough.
 */
@Composable
fun TGLoadingImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val media = LocalAppContainer.current.media
    val context = LocalContext.current
    val loadingDescription = stringResource(R.string.ds_image_loading)
    val url = remember(path) { media.resolve(path) }
    var attempt by remember(url) { mutableIntStateOf(0) }
    Box(modifier.background(TG.colors.card)) {
        if (url == null) {
            ImageUnavailable(Modifier.matchParentSize(), contentDescription, onRetry = null)
        } else {
            // Coil 2 does not cache failures, so a new request (different parameter) retries the download.
            val request = remember(url, attempt) {
                ImageRequest.Builder(context).data(url).setParameter("retry", attempt).crossfade(TGImageState.crossfade).build()
            }
            SubcomposeAsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.matchParentSize(),
                loading = {
                    Box(Modifier.fillMaxSize().semantics { this.contentDescription = loadingDescription }, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(TGImageState.spinner), color = TG.colors.accent, strokeWidth = TGImageState.spinnerStroke)
                    }
                },
                error = { ImageUnavailable(Modifier.fillMaxSize(), contentDescription, onRetry = { attempt++ }) },
            )
        }
    }
}

/** Error state: icon and caption, tappable to retry when [onRetry] is given (48dp minimum target). */
@Composable
private fun ImageUnavailable(modifier: Modifier, description: String?, onRetry: (() -> Unit)?) {
    val label = stringResource(if (onRetry != null) R.string.ds_image_failed_retry_cd else R.string.ds_image_unavailable)
    val retryLabel = stringResource(R.string.ds_image_retry_label)
    BoxWithConstraints(
        modifier.then(
            if (onRetry != null) Modifier.clickable(role = Role.Button, onClickLabel = retryLabel, onClick = onRetry) else Modifier,
        ).semantics { contentDescription = if (description.isNullOrBlank()) label else "$description. $label" },
        contentAlignment = Alignment.Center,
    ) {
        val showCaption = maxHeight >= TGImageState.captionMinHeight
        Column(
            Modifier.padding(TGSpace.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TGSpace.xs),
        ) {
            Icon(
                if (onRetry != null) Icons.Filled.Refresh else Icons.Filled.BrokenImage, contentDescription = null,
                tint = TG.colors.textSecondary, modifier = Modifier.size(TGImageState.retryIcon),
            )
            if (showCaption) {
                Text(
                    stringResource(if (onRetry != null) R.string.ds_image_load_failed_caption else R.string.ds_image_unavailable),
                    style = TGText.sailecMedium(14), color = TG.colors.textSecondary, textAlign = TextAlign.Center,
                )
            }
        }
    }
}
