package app.topgun.android.features.settings

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

/** One launcher icon: [name] is the iOS label (`ChangeIconView.AppIcon.rawValue`); index 0 is the default. */
internal class AppIconOption(val name: String, val preview: Int)

internal val appIconOptions = listOf(
    AppIconOption("Default", R.drawable.icon_preview_0),
    AppIconOption("AppIcon1", R.drawable.icon_preview_1),
    AppIconOption("AppIcon2", R.drawable.icon_preview_2),
    AppIconOption("AppIcon3", R.drawable.icon_preview_3),
    AppIconOption("AppIcon4", R.drawable.icon_preview_4),
)

/** iOS `ChangeIconView` ("Choose App Icon" list with a check on the current one). Index 0 is the default icon. */
@Composable
fun AppIconScreen(selected: Int, onBack: () -> Unit, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(settingsBackground())) {
        AccountTopBar("App Icons", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            Text("Choose App Icon", style = TGText.sailecRegular(12), color = TG.colors.textSecondary, modifier = Modifier.padding(horizontal = TGSpace.l))
            Column(Modifier.fillMaxWidth().clip(TGRadius.card).background(TG.colors.card)) {
                appIconOptions.forEachIndexed { index, icon ->
                    val isSelected = index == selected
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(index) })
                            .heightIn(min = TGAccountSize.rowMinHeight)
                            .padding(horizontal = TGSpace.l, vertical = TGSpace.s),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
                    ) {
                        Image(
                            painterResource(icon.preview), contentDescription = "${icon.name} app icon", contentScale = ContentScale.Crop,
                            modifier = Modifier.size(TGAccountSize.iconPreview).clip(TGAccountRadius.blogListImage),
                        )
                        Text(icon.name, style = TGText.sailecBold(17), color = TG.colors.textPrimary, modifier = Modifier.weight(1f))
                        Icon(
                            if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = if (isSelected) TGAccountColors.systemGreen else TG.colors.textPrimary,
                        )
                    }
                    if (index < appIconOptions.lastIndex) HorizontalDivider(Modifier.padding(start = TGSpace.l), color = TGBrand.gray.copy(alpha = 0.4f))
                }
            }
            // Android has no API to change the icon directly: the launcher refreshes on its own schedule.
            Text(
                "It can take a few seconds for your launcher to update.",
                style = TGText.sailecRegular(12), color = TG.colors.textSecondary, modifier = Modifier.padding(horizontal = TGSpace.l),
            )
        }
    }
}
