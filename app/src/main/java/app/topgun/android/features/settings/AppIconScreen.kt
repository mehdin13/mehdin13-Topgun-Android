package app.topgun.android.features.settings

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import app.topgun.android.R
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountColors
import app.topgun.android.designsystem.TGAccountRadius
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AccountTopBar

/** One launcher icon: [number] follows the iOS label (`ChangeIconView.AppIcon.rawValue`); index 0 is the default. */
internal class AppIconOption(val number: Int, val preview: Int) {
    /** 0 is the default icon; the others are numbered like the iOS `AppIcon1..4` names. */
    @Composable
    fun label(): String = if (number == 0) stringResource(R.string.settings_app_icon_default) else stringResource(R.string.settings_app_icon_numbered, number)
}

internal val appIconOptions = listOf(
    AppIconOption(0, R.drawable.icon_preview_0),
    AppIconOption(1, R.drawable.icon_preview_1),
    AppIconOption(2, R.drawable.icon_preview_2),
    AppIconOption(3, R.drawable.icon_preview_3),
    AppIconOption(4, R.drawable.icon_preview_4),
)

/** iOS `ChangeIconView` ("Choose App Icon" list with a check on the current one). Index 0 is the default icon. */
@Composable
fun AppIconScreen(selected: Int, onBack: () -> Unit, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(settingsBackground())) {
        AccountTopBar(stringResource(R.string.settings_app_icons_title), onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            Text(stringResource(R.string.settings_choose_app_icon), style = TGText.sailecRegular(12), color = TG.colors.textSecondary, modifier = Modifier.padding(horizontal = TGSpace.l))
            Column(Modifier.fillMaxWidth().clip(TGRadius.card).background(TG.colors.card)) {
                appIconOptions.forEachIndexed { index, icon ->
                    val isSelected = index == selected
                    val iconLabel = icon.label()
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(index) })
                            .heightIn(min = TGAccountSize.rowMinHeight)
                            .padding(horizontal = TGSpace.l, vertical = TGSpace.s),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
                    ) {
                        Image(
                            painterResource(icon.preview), contentDescription = stringResource(R.string.settings_app_icon_description, iconLabel), contentScale = ContentScale.Crop,
                            modifier = Modifier.size(TGAccountSize.iconPreview).clip(TGAccountRadius.blogListImage),
                        )
                        Text(iconLabel, style = TGText.sailecBold(17), color = TG.colors.textPrimary, modifier = Modifier.weight(1f))
                        Icon(
                            if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = stringResource(if (isSelected) R.string.selected else R.string.not_selected),
                            tint = if (isSelected) TGAccountColors.systemGreen else TG.colors.textPrimary,
                        )
                    }
                    if (index < appIconOptions.lastIndex) HorizontalDivider(Modifier.padding(start = TGSpace.l), color = TGBrand.gray.copy(alpha = 0.4f))
                }
            }
            // Android has no API to change the icon directly: the launcher refreshes on its own schedule.
            Text(
                stringResource(R.string.settings_app_icon_hint),
                style = TGText.sailecRegular(12), color = TG.colors.textSecondary, modifier = Modifier.padding(horizontal = TGSpace.l),
            )
        }
    }
}
