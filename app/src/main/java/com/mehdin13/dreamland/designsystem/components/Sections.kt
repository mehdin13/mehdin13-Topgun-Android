package com.mehdin13.dreamland.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLRadius
import com.mehdin13.dreamland.designsystem.DLSpacing

/** Section title with an optional trailing action ("See all"). */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = DL.type.titleMedium, color = DL.colors.textPrimary, modifier = Modifier.semantics { heading() })
        if (actionLabel != null) DLButton(actionLabel, onClick = onAction, variant = DLButtonVariant.Tertiary, size = DLButtonSize.Small)
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier, message: String? = null) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = DL.colors.accentPrimary)
        if (message != null) {
            Spacer(Modifier.height(DLSpacing.space12))
            Text(message, style = DL.type.bodySmall, color = DL.colors.textSecondary, textAlign = TextAlign.Center)
        }
    }
}

/** Friendly failure state with an optional retry. Never shows raw server or transport details. */
@Composable
fun ErrorView(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    val c = DL.colors
    Column(
        modifier
            .fillMaxWidth()
            .background(c.errorSoft, DLRadius.mdShape)
            .padding(DLSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DLIcon(DLIconName.Alert, tint = c.error)
        Text(message, style = DL.type.bodyRegular, color = c.textPrimary, textAlign = TextAlign.Center)
        if (onRetry != null) DLButton("Try again", onClick = onRetry, variant = DLButtonVariant.Secondary, size = DLButtonSize.Medium, icon = DLIconName.Refresh)
    }
}

@Composable
fun EmptyStateView(
    icon: DLIconName,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val c = DL.colors
    Column(
        modifier.fillMaxWidth().padding(DLSpacing.space32),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DLIcon(icon, tint = c.accentSecondaryText, size = com.mehdin13.dreamland.designsystem.DLSize.iconL)
        Text(title, style = DL.type.titleMedium, color = c.textPrimary, textAlign = TextAlign.Center)
        Text(message, style = DL.type.bodyRegular, color = c.textSecondary, textAlign = TextAlign.Center)
        if (actionLabel != null) DLButton(actionLabel, onClick = onAction)
    }
}
