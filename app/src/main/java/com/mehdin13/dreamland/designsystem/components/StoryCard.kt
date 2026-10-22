package com.mehdin13.dreamland.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLRadius
import com.mehdin13.dreamland.designsystem.DLSpacing

enum class StoryCardLayout { Tile, Row }

/** Favorite state shown on a card. [count] comes from the server. */
data class FavoriteState(val isFavorited: Boolean, val count: Int, val isUpdating: Boolean = false)

/** Story card in tile (grid) or row (list) layout. Favorite uses the shared [FavoriteButton]. */
@Composable
fun StoryCard(
    title: String,
    coverUrl: String?,
    seed: Int,
    modifier: Modifier = Modifier,
    meta: String? = null,
    layout: StoryCardLayout = StoryCardLayout.Tile,
    favorite: FavoriteState? = null,
    onToggleFavorite: () -> Unit = {},
    onOpen: () -> Unit,
) {
    val c = DL.colors
    Surface(
        modifier = modifier.clickable(role = Role.Button, onClick = onOpen),
        shape = DLRadius.lgShape,
        color = c.surfaceCard,
        border = BorderStroke(1.dp, c.borderSubtle),
        shadowElevation = 2.dp,
    ) {
        Box {
            if (layout == StoryCardLayout.Tile) {
                Column(Modifier.padding(DLSpacing.space12), verticalArrangement = Arrangement.spacedBy(DLSpacing.space8)) {
                    StoryCover(imageUrl = coverUrl, title = title, seed = seed)
                    Column(verticalArrangement = Arrangement.spacedBy(DLSpacing.space2)) {
                        Text(title, style = DL.type.headline, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (meta != null) Text(meta, style = DL.type.metadataLabel, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else {
                Row(
                    Modifier.padding(DLSpacing.space12).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DLSpacing.space12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StoryCover(imageUrl = coverUrl, title = title, seed = seed, modifier = Modifier.width(72.dp), corner = DLRadius.sm)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DLSpacing.space2)) {
                        Text(title, style = DL.type.headline, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (meta != null) Text(meta, style = DL.type.metadataLabel, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (favorite != null) Box(Modifier.width(DLSpacing.space48 + DLSpacing.space8))
                }
            }
            if (favorite != null) {
                FavoriteButton(
                    isFavorited = favorite.isFavorited,
                    count = favorite.count,
                    onToggle = onToggleFavorite,
                    enabled = !favorite.isUpdating,
                    modifier = Modifier
                        .align(if (layout == StoryCardLayout.Tile) Alignment.TopEnd else Alignment.CenterEnd)
                        .padding(DLSpacing.space8),
                )
            }
        }
    }
}
