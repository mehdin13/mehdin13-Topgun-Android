package com.mehdin13.dreamland.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLRadius
import com.mehdin13.dreamland.designsystem.DLSize
import com.mehdin13.dreamland.designsystem.DLSpacing

/**
 * The one favorite control used everywhere. Shows the server's [count] (never a local one).
 * The caller toggles optimistically and rolls back on failure; pass [enabled] = false while the request is in flight.
 */
@Composable
fun FavoriteButton(
    isFavorited: Boolean,
    count: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = DL.colors
    val tint = if (isFavorited) c.accentFavoriteText else c.textSecondary
    Row(
        modifier = modifier
            .defaultMinSize(minWidth = DLSize.tapMin, minHeight = DLSize.tapMin)
            .clickable(enabled = enabled, role = Role.Button, onClick = onToggle)
            .semantics(mergeDescendants = true) {
                contentDescription = "Favorite, $count ${if (count == 1) "favorite" else "favorites"}"
                stateDescription = if (isFavorited) "Favorited" else "Not favorited"
            }
            .padding(horizontal = DLSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(DLSpacing.space4, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DLIcon(DLIconName.Heart, tint = if (enabled) tint else c.disabled, size = DLSize.iconS, filled = isFavorited)
        Text(text = count.toString(), style = DL.type.metadataLabel, color = if (enabled) tint else c.disabled)
    }
}
