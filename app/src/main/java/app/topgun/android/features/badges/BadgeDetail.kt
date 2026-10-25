package app.topgun.android.features.badges

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.R
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountColors
import app.topgun.android.designsystem.TGAccountRadius
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.TGImage
import app.topgun.core.model.Badge

/**
 * iOS `BadgeDetailView`: a 300x400 rounded card (background art, badge image) that tilts and shifts with a drag
 * (parallax: background 1/10, badge 1/5), and the badge name under it. Drag resets on release.
 */
@Composable
fun BadgeDetail(badge: Badge) {
    var drag by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(TGSpace.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(TGAccountSize.badgeCardWidth, TGAccountSize.badgeCardHeight)
                .graphicsLayer {
                    translationX = drag.x / 10f
                    translationY = drag.y / 10f
                    // iOS rotation3DEffect: 10 degrees about the axis (-dy, dx).
                    if (dragging) {
                        rotationX = if (drag.y == 0f) 0f else -ROTATE_DEGREES * (drag.y / maxOf(kotlin.math.abs(drag.y), kotlin.math.abs(drag.x)))
                        rotationY = if (drag.x == 0f) 0f else ROTATE_DEGREES * (drag.x / maxOf(kotlin.math.abs(drag.y), kotlin.math.abs(drag.x)))
                    }
                }
                .shadow(TGAccountSize.badgeCardShadow, TGAccountRadius.badgeCard, ambientColor = TGAccountColors.systemPurple.copy(alpha = 0.2f), spotColor = TGBrand.primaryRed.copy(alpha = 0.1f))
                .clip(TGAccountRadius.badgeCard)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = { drag = Offset.Zero; dragging = false },
                        onDragCancel = { drag = Offset.Zero; dragging = false },
                    ) { change, amount -> change.consume(); drag += amount }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.requiredSize(TGAccountSize.badgeCardBackWidth, TGAccountSize.badgeCardBackHeight).graphicsLayer { translationX = drag.x / 10f; translationY = drag.y / 10f }) {
                if (badge.cover.isNullOrBlank()) {
                    Image(painterResource(R.drawable.back4), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    TGImage(badge.cover, contentDescription = null, modifier = Modifier.fillMaxSize())
                }
            }
            TGImage(
                badge.icon, contentDescription = stringResource(R.string.badges_badge, badge.name.orEmpty()),
                modifier = Modifier.size(TGAccountSize.badgeCardImage).graphicsLayer { translationX = drag.x / 5f; translationY = drag.y / 5f },
                contentScale = ContentScale.Fit,
            )
        }
        androidx.compose.material3.Text(
            badge.name.orEmpty(), style = TGText.rounded(30), color = TG.colors.textPrimary, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = TGSpace.m, bottom = TGSpace.s),
        )
    }
}

private const val ROTATE_DEGREES = 10f
