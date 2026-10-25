package app.topgun.android.features.settings

import androidx.compose.ui.res.stringResource
import app.topgun.android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Appearance
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AccountTopBar

/** iOS `ThemeView`: inline picker System / Light / Dark. The choice is stored in [app.topgun.android.data.AppPreferences]. */
@Composable
fun ThemeScreen(onBack: () -> Unit, onSelect: (Appearance) -> Unit) {
    val appearance by LocalAppContainer.current.prefs.appearance.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(settingsBackground())) {
        AccountTopBar(stringResource(R.string.settings_theme_title), onBack)
        Column(Modifier.padding(TGSpace.l).fillMaxWidth().clip(TGRadius.card).background(TG.colors.card)) {
            Appearance.entries.forEachIndexed { index, theme ->
                val selected = theme == appearance
                Row(
                    Modifier.fillMaxWidth()
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(theme) })
                        .heightIn(min = TGAccountSize.rowMinHeight)
                        .padding(horizontal = TGSpace.l),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(
                            when (theme) {
                                Appearance.System -> R.string.settings_appearance_system
                                Appearance.Light -> R.string.settings_appearance_light
                                Appearance.Dark -> R.string.settings_appearance_dark
                            },
                        ),
                        style = TGText.sailecRegular(17),
                        color = if (selected) TG.colors.accent else TG.colors.textPrimary,
                    )
                    if (selected) Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.selected), tint = TG.colors.accent)
                }
                if (index < Appearance.entries.lastIndex) HorizontalDivider(Modifier.padding(start = TGSpace.l), color = TGBrand.gray.copy(alpha = 0.4f))
            }
        }
    }
}

/** iOS: Form/list background is `formColor` in light mode and `primary_color` in dark. */
@Composable
internal fun settingsBackground() = if (TG.colors.isDark) TG.colors.background else TGBrand.formGray
