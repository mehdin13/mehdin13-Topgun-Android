package app.topgun.android.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import app.topgun.android.R
import app.topgun.android.data.AppLanguage
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountColors
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AccountTopBar

/** Language picker: "System default" plus every [AppLanguage] in its own script. Selecting one recreates the activity. */
@Composable
fun LanguageScreen(selected: AppLanguage?, onBack: () -> Unit, onSelect: (AppLanguage?) -> Unit) {
    Column(Modifier.fillMaxSize().background(settingsBackground())) {
        AccountTopBar(stringResource(R.string.language_title), onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            val options: List<AppLanguage?> = listOf(null) + AppLanguage.entries
            Column(Modifier.fillMaxWidth().clip(TGRadius.card).background(TG.colors.card)) {
                options.forEachIndexed { index, language ->
                    val isSelected = language == selected
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(language) })
                            .heightIn(min = TGAccountSize.rowMinHeight)
                            .padding(horizontal = TGSpace.l, vertical = TGSpace.s),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
                    ) {
                        Text(
                            language?.nativeName ?: stringResource(R.string.language_system_default),
                            style = TGText.sailecBold(17), color = TG.colors.textPrimary, modifier = Modifier.weight(1f),
                        )
                        Icon(
                            if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = stringResource(if (isSelected) R.string.selected else R.string.not_selected),
                            tint = if (isSelected) TGAccountColors.systemGreen else TG.colors.textPrimary,
                        )
                    }
                    if (index < options.lastIndex) HorizontalDivider(Modifier.padding(start = TGSpace.l), color = TGBrand.gray.copy(alpha = 0.4f))
                }
            }
        }
    }
}
