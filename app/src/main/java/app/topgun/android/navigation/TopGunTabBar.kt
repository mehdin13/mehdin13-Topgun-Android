package app.topgun.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand

private class TabItem(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val Tabs = listOf(
    TabItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
    TabItem("Favorites", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    TabItem("Settings", Icons.Outlined.Settings, Icons.Filled.Settings),
)

/** Plain Material 3 bottom navigation bar: always visible, no animation beyond the default indicator. */
@Composable
fun TopGunTabBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier, containerColor = TG.colors.card) {
        Tabs.forEachIndexed { index, tab ->
            val on = index == selected
            NavigationBarItem(
                selected = on,
                onClick = { onSelect(index) },
                icon = { Icon(if (on) tab.selectedIcon else tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TGBrand.tabIdle,
                    selectedTextColor = TG.colors.textPrimary,
                    indicatorColor = TGBrand.yellow,
                    unselectedIconColor = TG.colors.textPrimary.copy(alpha = 0.6f),
                    unselectedTextColor = TG.colors.textPrimary.copy(alpha = 0.6f),
                ),
            )
        }
    }
}
