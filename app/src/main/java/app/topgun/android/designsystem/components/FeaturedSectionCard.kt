package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import app.topgun.android.designsystem.TGHomeColors
import app.topgun.android.designsystem.TGHomeSize
import app.topgun.android.designsystem.TGHomeText
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSpace

/**
 * "Welcome on board!" card (iOS `FeaturedSectionCardView`): 220x120 diagonal gradient, icon in a translucent
 * circle at the top, bold title and a smaller subtitle at the bottom.
 */
@Composable
fun FeaturedSectionCard(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    gradient: Pair<Color, Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .size(TGHomeSize.featureCardWidth, TGHomeSize.featureCardHeight)
            .clip(TGRadius.image)
            .background(Brush.linearGradient(listOf(gradient.first, gradient.second)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(TGSpace.l),
        verticalArrangement = Arrangement.spacedBy(TGSpace.xs),
    ) {
        Icon(
            icon, contentDescription = null, tint = TGHomeColors.white,
            modifier = Modifier
                .clip(CircleShape)
                .background(TGHomeColors.white.copy(alpha = TGHomeColors.featureIconAlpha))
                .padding(TGHomeSize.featureIconPadding)
                .size(TGHomeSize.featureIcon),
        )
        Spacer(Modifier.weight(1f))
        Text(title, style = TGHomeText.headline, color = TGHomeColors.white, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (subtitle != null) {
            Text(
                subtitle, style = TGHomeText.caption,
                color = TGHomeColors.white.copy(alpha = TGHomeColors.featureSubtitleAlpha),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
