package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGEntry
import app.topgun.android.designsystem.TGEntryColors
import app.topgun.android.designsystem.TGSize

/**
 * Black capsule button of the intro ("Next", "Continue"): white semibold label, 15 pt vertical padding.
 * In dark mode the capsule uses the accent color because black would vanish on the dark background.
 */
@Composable
fun EntryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = if (TG.colors.isDark) TG.colors.accent else TGEntryColors.black,
    contentColor: Color = TGEntryColors.white,
) {
    Box(
        modifier
            .heightIn(min = TGSize.tapMin)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(vertical = TGEntry.buttonVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TGEntry.capsuleLabelStyle, color = contentColor)
    }
}
