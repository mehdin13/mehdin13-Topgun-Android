package app.topgun.android.designsystem.components

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.topgun.android.R
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAsset
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGGeoColor
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import coil.compose.AsyncImage

/**
 * Shared pieces of the country, manufacturer and curated-list screens (iOS `FeaturedCardItem.swift`,
 * `CountryView.swift`, `ManufacturerView.swift`).
 */

/** Top bar of a pushed screen: 48dp back button, one-line title, optional trailing [actions] (iOS nav bar). */
@Composable
fun GeoTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = TGSpace.s, vertical = TGSpace.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
    ) {
        GeoIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ds_back), onBack, tint = TG.colors.accent)
        Text(
            title, style = TGText.sailecBold(18), color = TG.colors.textPrimary,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/** 48dp round icon button with a content description. */
@Composable
fun GeoIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier.size(TGSize.tapMin).clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint)
    }
}

/**
 * Round dark button floating over a cover or map (iOS `arrow.backward.circle.fill` and the globe/map toggle):
 * 48dp touch area, [TGGeo.overlayButton] visible circle.
 */
@Composable
fun GeoOverlayButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.size(TGSize.tapMin).clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(TGGeo.overlayButton).clip(CircleShape).background(Color.Black.copy(alpha = TGGeo.scrim)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = Color.White)
        }
    }
}

/** Gradient + default cover artwork shown when an image is missing (iOS `defaultCoverPlaceholder`). */
@Composable
fun GeoCoverPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier.background(
            Brush.linearGradient(listOf(TGBrand.blue.copy(alpha = TGGeo.scrim), TGGeoColor.purple.copy(alpha = TGGeo.scrim))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painterResource(R.drawable.top_gun_default_cover), contentDescription = null,
            contentScale = ContentScale.Crop, alpha = TGGeo.scrimLight, modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Cover photo from an API path, over the default placeholder. */
@Composable
fun GeoCover(path: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    Box(modifier) {
        GeoCoverPlaceholder(Modifier.matchParentSize())
        if (!path.isNullOrBlank()) TGImage(path, contentDescription, Modifier.matchParentSize())
    }
}

/**
 * Country flag: the API's flag/icon image when there is one, else the bundled flag SVG for the ISO [code],
 * else the default Top Gun icon.
 */
@Composable
fun GeoFlag(
    path: String?,
    code: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val local = TGAsset.flagSvg(code)
    when {
        !path.isNullOrBlank() -> TGImage(path, contentDescription, modifier, contentScale)
        local != null -> AsyncImage(local, contentDescription, modifier, contentScale = contentScale)
        else -> Image(painterResource(R.drawable.top_gun_default_icon), contentDescription, modifier, contentScale = contentScale)
    }
}

/**
 * Card of the country grid and the manufacturer carousels (iOS `FeaturedCardItem`): 16:9 cover under a dark scrim,
 * the flag or logo in the middle, and the name (two lines) below.
 */
@Composable
fun FeaturedCard(
    name: String,
    iconPath: String?,
    coverPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    code: String? = null,
) {
    Column(
        modifier.clickable(role = Role.Button, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(TGSpace.s),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(TGGeo.shape20), contentAlignment = Alignment.Center) {
            GeoCover(coverPath, null, Modifier.matchParentSize())
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = TGGeo.scrimLight)))
            GeoFlag(iconPath, code, stringResource(R.string.ds_flag_cd, name), Modifier.size(TGGeo.featuredFlagWidth, TGGeo.featuredFlagHeight))
        }
        Text(
            name, style = TGText.sailecBold(12), color = TG.colors.textPrimary, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().heightIn(min = TGGeo.featuredNameHeight).padding(horizontal = TGSpace.xs),
        )
    }
}

/**
 * Small card of the horizontal equipment grids on the country and manufacturer pages (iOS `EquipmentCard`):
 * 160 x 120 thumbnail (with a lock when [isLocked]) and a two-line name on a faint background.
 */
@Composable
fun EquipmentCard(
    name: String,
    thumbnail: String?,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.size(width = TGGeo.equipmentCardWidth, height = TGGeo.equipmentImageHeight + TGSpace.s + TGGeo.equipmentNameHeight)
            .clip(TGGeo.shape12).background(TG.colors.textPrimary.copy(alpha = 0.05f))
            .clickable(role = Role.Button, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(TGSpace.s),
    ) {
        Box(Modifier.size(TGGeo.equipmentCardWidth, TGGeo.equipmentImageHeight).clip(TGGeo.shape12)) {
            if (thumbnail.isNullOrBlank()) {
                Image(
                    painterResource(R.drawable.img_0_placeholder), contentDescription = stringResource(R.string.ds_photo_cd, name),
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
                )
            } else {
                TGImage(thumbnail, stringResource(R.string.ds_photo_cd, name), Modifier.fillMaxSize())
            }
            if (isLocked) LockScrim(Modifier.matchParentSize(), TGGeo.scrim)
        }
        Text(
            name, style = TGText.sailecBold(14), color = TG.colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().height(TGGeo.equipmentNameHeight).padding(horizontal = TGSpace.xs),
        )
    }
}

/** Dark overlay with a lock icon for Pro-only aircraft. */
@Composable
fun LockScrim(modifier: Modifier = Modifier, alpha: Float = TGGeo.scrim) {
    Box(modifier.background(Color.Black.copy(alpha = alpha)), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.ds_pro_only), tint = Color.White)
    }
}

/** Small tinted pill button (iOS `.buttonStyle(.bordered).controlSize(.small).tint(...)`), 48dp tall touch area. */
@Composable
fun GeoLinkButton(text: String, icon: ImageVector, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.heightIn(min = TGSize.tapMin).clip(TGRadius.button).background(tint.copy(alpha = TGGeo.tint))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = TGSpace.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TGSpace.xs),
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(text, style = TGText.sailecMedium(14), color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
