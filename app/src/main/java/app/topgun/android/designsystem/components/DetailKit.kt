package app.topgun.android.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGDetailSpace
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText

/*
 * Shared pieces of the aircraft detail screen (iOS DetailsView+Components.swift, HeadingView, CommentsListView).
 * Kept here so other screens (for example the manufacturer and country screens) can reuse them.
 */

/** iOS `CircleButtonStyle`: 40pt white circle with a black glyph. The touch target is 48dp. */
@Composable
fun DetailCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: Color = TGDetailColors.photoBlack,
    background: Color = TGDetailColors.onPhoto,
) {
    Box(
        modifier
            .size(TGSize.tapMin)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(TGDetailSize.circleButton).background(background, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = glyph, modifier = Modifier.size(TGDetailSize.circleButton - TGSpace.l))
        }
    }
}

/** The 13pt rounded tag under the aircraft title (country, generation, manufacturer): [tint] text on a tinted background. */
@Composable
fun DetailPill(text: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier, tintAlpha: Float = 0.1f) {
    Row(
        modifier
            .background(tint.copy(alpha = tintAlpha), TGDetailSize.pill)
            .padding(horizontal = TGDetailSpace.pillH, vertical = TGDetailSpace.pillV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TGDetailSpace.six),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(TGSpace.m))
        Text(text, style = TGText.sailecRegular(13), color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** iOS `HeadingView`: small asset icon and a bold title. [iconRes] is an `R.drawable` id or null. */
@Composable
fun DetailHeading(text: String, iconRes: Int?, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        if (iconRes != null) {
            Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(TGSpace.xl))
        }
        Text(text, style = TGText.sailecBold(20), color = TG.colors.textPrimary)
    }
}

/** iOS `AllSettingsItem`: a gradient tile with an icon, a title and an optional small caps subtitle. */
@Composable
fun DetailActionTile(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    gradient: List<Color>,
) {
    Row(
        modifier
            .height(TGDetailSize.tileHeight)
            .clip(TGDetailSize.tileRadius)
            .background(Brush.linearGradient(gradient))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(TGSpace.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TGDetailSpace.hair),
    ) {
        Box(Modifier.size(TGDetailSize.tileIcon), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TGDetailColors.onPhoto)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TGDetailSpace.hair)) {
            Text(text, style = TGText.sailecBold(16), color = TGDetailColors.onPhoto, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotEmpty()) {
                Text(subtitle.uppercase(), style = TGText.sailecMedium(10), color = TGDetailColors.onPhoto, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** iOS `DetailsInfoView`: a card with a title and a gray value, used in the General Details grid. */
@Composable
fun DetailInfoCell(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(TGDetailSize.cellRadius)
            .background(TG.colors.card)
            .border(TGDetailSize.hairlineWidth, TGDetailColors.hairline, TGDetailSize.cellRadius)
            .padding(vertical = TGSpace.m, horizontal = TGSpace.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TGSpace.s),
    ) {
        Text(title, style = TGText.sailecMedium(16), color = TG.colors.textPrimary, textAlign = TextAlign.Center)
        Text(value, style = TGText.sailecRegular(12), color = TGDetailColors.infoText, textAlign = TextAlign.Center)
    }
}

/**
 * Five stars. With [onRate] the stars are buttons (iOS `ratingSection`, 30pt, yellow when filled, faint white when empty
 * on the dark rating card); without it they only display [filled] (iOS `StarRatingView(isInteractive: false)`).
 */
@Composable
fun DetailStars(
    filled: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    onRate: ((Int) -> Unit)? = null,
    enabled: Boolean = true,
    emptyTint: Color = TGBrand.gray,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(if (onRate != null) TGSpace.xs else TGDetailSpace.zero), verticalAlignment = Alignment.CenterVertically) {
        for (index in 1..5) {
            val on = index <= filled
            val star: @Composable () -> Unit = {
                Icon(
                    if (on) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (on) TGBrand.yellow else emptyTint,
                    modifier = Modifier.size(size),
                )
            }
            if (onRate != null) {
                Box(
                    Modifier
                        .defaultMinSize(minWidth = TGSize.tapMin, minHeight = TGSize.tapMin)
                        .clip(CircleShape)
                        .clickable(enabled = enabled, role = Role.Button) { onRate(index) }
                        .semantics { this.contentDescription = "Rate $index of 5 stars" },
                    contentAlignment = Alignment.Center,
                ) { star() }
            } else {
                star()
            }
        }
    }
}
