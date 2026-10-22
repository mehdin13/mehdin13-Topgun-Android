package app.topgun.android.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGEntry
import app.topgun.android.designsystem.TGEntryColors
import app.topgun.android.designsystem.TGSpace

/** White pill with the provider logo and label (iOS `SocialLoginButton`). Shows a spinner while [loading]. */
@Composable
fun SocialLoginButton(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val shape = RoundedCornerShape(TGEntry.socialButtonRadius)
    Box(
        modifier
            .fillMaxWidth()
            .height(TGEntry.socialButtonHeight)
            .shadow(TGEntry.socialButtonShadow, shape)
            .clip(shape)
            .background(TGEntryColors.white)
            .clickable(enabled = !loading, onClick = onClick)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = TGSpace.l), verticalAlignment = Alignment.CenterVertically) {
            Image(icon, contentDescription = null, modifier = Modifier.size(TGEntry.socialIcon))
            Text(
                text,
                style = TGEntry.socialLabelStyle,
                color = TGEntryColors.black,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            // Keeps the label centered: same width as the icon.
            Box(Modifier.size(TGEntry.socialIcon), contentAlignment = Alignment.Center) {
                if (loading) CircularProgressIndicator(color = TG.colors.accent, strokeWidth = TGSpace.xs / 2, modifier = Modifier.size(TGEntry.socialIcon))
            }
        }
    }
}
