package app.topgun.android.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText

/** A titled group of rows on a card, like an iOS inset-grouped `Section`. */
@Composable
fun SettingsSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth().padding(horizontal = TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        Text(title, style = TGText.sailecRegular(13), color = TG.colors.textSecondary, modifier = Modifier.padding(start = TGSpace.s))
        Column(Modifier.fillMaxWidth().clip(TGRadius.card).background(TG.colors.card)) { content() }
    }
}

/**
 * One settings row: colored icon, title (+ optional caption), optional trailing content.
 * [onClick] null means the row is not tappable itself (a switch row handles its own tap).
 */
@Composable
fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleColor: Color = TG.colors.textPrimary,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .heightIn(min = TGAccountSize.rowMinHeight)
                .padding(horizontal = TGSpace.l, vertical = TGSpace.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(TGAccountSize.rowIcon))
            Column(Modifier.weight(1f)) {
                Text(title, style = TGText.sailecRegular(17), color = titleColor)
                if (subtitle != null) Text(subtitle, style = TGText.sailecRegular(12), color = TG.colors.textSecondary)
            }
            trailing?.invoke()
        }
        if (showDivider) HorizontalDivider(Modifier.padding(start = TGSpace.l + TGAccountSize.rowIcon + TGSpace.m), color = TGBrand.gray.copy(alpha = 0.4f))
    }
}

/** Disclosure chevron for rows that open another screen. */
@Composable
fun SettingsChevron() {
    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TG.colors.textSecondary)
}
