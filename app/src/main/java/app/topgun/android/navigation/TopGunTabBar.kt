package app.topgun.android.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.topgun.android.R
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGTabBar

private class TabItem(val label: Int, val icon: ImageVector, val selectedIcon: ImageVector)

private val Tabs = listOf(
    TabItem(R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home),
    TabItem(R.string.nav_favorites, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    TabItem(R.string.nav_settings, Icons.Outlined.Settings, Icons.Filled.Settings),
)

/**
 * Floating rounded tab bar (iOS `AnimatedTabBar`): white in dark mode, light blue-gray in light mode,
 * a yellow ball that springs to the selected tab, dimmed icons for the other tabs.
 * Fades/slides away while the list scrolls down ([visible] = false; iOS easeInOut 0.3).
 */
@Composable
fun TopGunTabBar(selected: Int, onSelect: (Int) -> Unit, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(TGTabBar.visibilityMillis)) { it } + fadeIn(tween(TGTabBar.visibilityMillis)),
        exit = slideOutVertically(tween(TGTabBar.visibilityMillis)) { it } + fadeOut(tween(TGTabBar.visibilityMillis)),
        modifier = modifier,
    ) {
        val barColor = if (TG.colors.isDark) androidx.compose.ui.graphics.Color.White else TGBrand.lightWhite
        // Fractional position of the ball in "tab slots"; spring slide, no bounce/morph.
        val slot by animateFloatAsState(
            targetValue = selected.toFloat(),
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "tabBall",
        )
        BoxWithConstraints(
            Modifier
                .navigationBarsPadding()
                .padding(start = TGTabBar.horizontalMargin, end = TGTabBar.horizontalMargin, bottom = TGTabBar.bottomMargin)
                .fillMaxWidth()
                .shadow(TGTabBar.shadow, TGRadius.tabBar)
                .clip(TGRadius.tabBar)
                .background(barColor)
                .padding(vertical = TGTabBar.verticalPadding),
        ) {
            val slotWidth = maxWidth / Tabs.size
            Box(
                Modifier
                    .offset(x = slotWidth * slot + (slotWidth - TGTabBar.ball) / 2)
                    .size(TGTabBar.ball)
                    .clip(CircleShape)
                    .background(TGBrand.yellow),
            )
            Box(Modifier.fillMaxWidth().height(TGTabBar.ball)) {
                Tabs.forEachIndexed { index, tab ->
                    val on = index == selected
                    Box(
                        Modifier
                            .offset(x = slotWidth * index)
                            .width(slotWidth)
                            .height(maxOf(TGTabBar.ball, TGSize.tapMin))
                            .selectable(selected = on, role = Role.Tab, onClick = { onSelect(index) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (on) tab.selectedIcon else tab.icon,
                            contentDescription = stringResource(tab.label),
                            modifier = Modifier.size(TGTabBar.icon),
                            tint = if (on) TGBrand.tabIdle else TGBrand.tabIdle.copy(alpha = TGTabBar.idleAlpha),
                        )
                    }
                }
            }
        }
    }
}
