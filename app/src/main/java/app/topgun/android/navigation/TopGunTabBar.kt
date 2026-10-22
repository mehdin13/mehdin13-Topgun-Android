package app.topgun.android.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize

private class TabItem(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val Tabs = listOf(
    TabItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
    TabItem("Favorites", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    TabItem("Settings", Icons.Outlined.Settings, Icons.Filled.Settings),
)

/**
 * Floating rounded tab bar (iOS `AnimatedTabBar`): white in dark mode, light blue-gray in light mode,
 * a yellow ball behind the selected icon. Slides away while the list scrolls down ([visible] = false).
 */
@Composable
fun TopGunTabBar(selected: Int, onSelect: (Int) -> Unit, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 40.dp, vertical = 16.dp)
                .fillMaxWidth()
                .clip(TGRadius.tabBar)
                .background(if (TG.colors.isDark) androidx.compose.ui.graphics.Color.White else TGBrand.lightWhite)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tabs.forEachIndexed { index, tab ->
                val on = index == selected
                Box(
                    Modifier
                        .size(TGSize.tapMin + 4.dp)
                        .clip(CircleShape)
                        .background(if (on) TGBrand.yellow else androidx.compose.ui.graphics.Color.Transparent)
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(index) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (on) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                        tint = if (on) TGBrand.tabIdle else TGBrand.tabIdle.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}
