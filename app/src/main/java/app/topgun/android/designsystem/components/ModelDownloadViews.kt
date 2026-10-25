package app.topgun.android.designsystem.components

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.platform.ModelDownloadState
import app.topgun.android.platform.formatByteCount

/** "Downloading 3D model… 42%" (no percentage while the server has not told the file size). */
@Composable
fun ModelDownloadState.Downloading.title(): String =
    percent?.let { stringResource(R.string.model_downloading_percent, it) } ?: stringResource(R.string.model_downloading)

/** "12.3 MB of 29.0 MB", or just the bytes so far when the total is unknown. */
@Composable
fun ModelDownloadState.Downloading.bytesLabel(): String =
    totalBytes?.let { stringResource(R.string.model_bytes_of, formatByteCount(bytesRead), formatByteCount(it)) } ?: formatByteCount(bytesRead)

/**
 * Determinate progress of a 3D model download: title with percentage, a progress bar (indeterminate when the size is
 * unknown) and the bytes so far. Used by the viewer's loading state and by [ModelOfflineCard].
 */
@Composable
fun ModelDownloadProgress(state: ModelDownloadState.Downloading, modifier: Modifier = Modifier) {
    val fraction = state.fraction
    val title = state.title()
    val bytes = state.bytesLabel()
    Column(
        modifier.semantics(mergeDescendants = true) { contentDescription = "$title. $bytes" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TGSpace.s),
    ) {
        Text(title, style = TGText.sailecBold(16), color = TG.colors.textPrimary, textAlign = TextAlign.Center)
        if (fraction != null) {
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth(), color = TG.colors.accent)
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = TG.colors.accent)
        }
        Text(bytes, style = TGText.sailecRegular(13), color = TG.colors.textSecondary, textAlign = TextAlign.Center)
    }
}

/**
 * Offline download controls of the aircraft 3D page: Download / progress with Cancel / "Downloaded ✓" with Remove /
 * failure with Retry. State and actions come from [app.topgun.android.platform.ModelDownloads].
 */
@Composable
fun ModelOfflineCard(
    state: ModelDownloadState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().background(TG.colors.card, TGRadius.card).padding(TGSpace.l),
        verticalArrangement = Arrangement.spacedBy(TGSpace.m),
    ) {
        when (state) {
            ModelDownloadState.NotDownloaded -> ActionRow(
                icon = Icons.Filled.Download, title = stringResource(R.string.model_download_title), subtitle = stringResource(R.string.model_download_subtitle),
            ) { ActionButton(stringResource(R.string.model_download), onDownload, filled = true) }

            is ModelDownloadState.Downloading -> {
                ModelDownloadProgress(state)
                TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally).defaultMinSize(minHeight = TGSize.tapMin)) {
                    Text(stringResource(R.string.ds_cancel), style = TGText.sailecMedium(16), color = TG.colors.accent)
                }
            }

            is ModelDownloadState.Downloaded -> ActionRow(
                icon = Icons.Filled.CheckCircle, title = stringResource(R.string.model_downloaded), subtitle = stringResource(R.string.model_saved_on_device, formatByteCount(state.bytes)),
                iconTint = TGDetailColors.manufacturerGreen,
            ) { ActionButton(stringResource(R.string.model_remove), onRemove, filled = false) }

            is ModelDownloadState.Failed -> ActionRow(
                icon = Icons.Filled.ErrorOutline, title = stringResource(R.string.model_download_failed), subtitle = state.message, iconTint = TGBrand.red,
            ) { ActionButton(stringResource(R.string.ds_retry), onDownload, filled = true) }
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color = TG.colors.accent,
    action: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.m)) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(TGDetailSize.specIcon))
        Column(Modifier.weight(1f)) {
            Text(title, style = TGText.sailecBold(16), color = TG.colors.textPrimary)
            Text(subtitle, style = TGText.sailecRegular(13), color = TG.colors.textSecondary)
        }
        action()
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit, filled: Boolean) {
    val modifier = Modifier.defaultMinSize(minHeight = TGSize.tapMin)
    if (filled) {
        Button(onClick, modifier, shape = TGDetailSize.buttonRadius) { Text(text, style = TGText.sailecMedium(16)) }
    } else {
        OutlinedButton(onClick, modifier, shape = TGDetailSize.buttonRadius) { Text(text, style = TGText.sailecMedium(16)) }
    }
}
