package app.topgun.android.designsystem.components

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.core.model.Aircraft

/**
 * The aircraft row used on Home, Favorites, custom lists and search (iOS `HomeListModelView`):
 * 100dp rounded thumbnail (with a dark lock overlay for VIP aircraft the user cannot open), name, maker,
 * and generation / country / years tags. [isLocked] is true for a VIP aircraft when the user is not Pro.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AircraftListCard(
    aircraft: Aircraft,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Long press (iOS `.contextMenu`); null = no long-press action. */
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val c = TG.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(TGRadius.card)
            .background(c.card)
            .combinedClickable(role = Role.Button, onLongClickLabel = onLongClickLabel, onLongClick = onLongClick, onClick = onClick)
            .padding(TGSpace.m),
        horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
    ) {
        Box(Modifier.size(TGSize.listThumbnail).clip(TGRadius.card)) {
            TGImage(aircraft.thumbnail, contentDescription = stringResource(R.string.ds_photo_cd, aircraft.name), modifier = Modifier.size(TGSize.listThumbnail))
            if (isLocked) {
                Box(Modifier.size(TGSize.listThumbnail).background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.ds_pro_only), tint = Color.White)
                }
            }
        }
        Column(Modifier.weight(1f).padding(vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            Text(aircraft.name, style = TGText.sailecBold(16), color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                aircraft.manufacturers.firstOrNull()?.name ?: stringResource(R.string.ds_no_description),
                style = TGText.sailecMedium(10), color = c.textPrimary.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                aircraft.generation?.takeIf { it.isNotEmpty() }?.let { TagChip(it, Icons.Filled.Bolt, TGBrand.red) }
                TagChip(aircraft.countries.firstOrNull()?.name ?: stringResource(R.string.ds_unknown_location), Icons.Outlined.Public, TGBrand.blue)
                aircraft.produced?.takeIf { it.isNotEmpty() }?.let { TagChip(it, Icons.Outlined.CalendarMonth, TGBrand.orange) }
            }
        }
    }
}
