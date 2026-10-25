package app.topgun.android.designsystem.components

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText

/** "Loading aircraft..." centered spinner used for first loads. */
@Composable
fun LoadingState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().heightIn(min = 200.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = TG.colors.accent)
        Text(message, style = TGText.body, color = TG.colors.textSecondary, modifier = Modifier.padding(top = TGSpace.m))
    }
}

/** Empty or error state with an optional retry button (iOS `EmptyStateView`). */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Search,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().heightIn(min = 200.dp).padding(TGSpace.xl),
        verticalArrangement = Arrangement.spacedBy(TGSpace.m, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = TG.colors.textSecondary)
        Text(message, style = TGText.sailecMedium(16), color = TG.colors.textPrimary, textAlign = TextAlign.Center)
        if (onRetry != null) Button(onClick = onRetry) { Text(stringResource(R.string.ds_try_again)) }
    }
}

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) =
    EmptyState(message, modifier, Icons.Outlined.Warning, onRetry)
