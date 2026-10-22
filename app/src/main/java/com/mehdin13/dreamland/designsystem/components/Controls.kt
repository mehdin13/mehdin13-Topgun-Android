package com.mehdin13.dreamland.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLRadius
import com.mehdin13.dreamland.designsystem.DLSize
import com.mehdin13.dreamland.designsystem.DLSpacing

/** Selectable chip (pill radius, per the design: chips and segmented controls only). */
@Composable
fun DLChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: DLIconName? = null) {
    val c = DL.colors
    val fg = if (selected) c.textOnAccent else c.textPrimary
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = DLSize.tapMin)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        shape = DLRadius.pillShape,
        color = if (selected) c.accentPrimary else c.surfaceCard,
        border = BorderStroke(1.dp, if (selected) c.accentPrimary else c.borderSubtle),
    ) {
        Row(
            Modifier.padding(horizontal = DLSpacing.space16),
            horizontalArrangement = Arrangement.spacedBy(DLSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) DLIcon(icon, tint = fg, size = DLSize.iconS)
            Text(label, style = DL.type.bodySmall, color = fg)
        }
    }
}

/** Segmented control on a `backgroundSecondary` track. */
@Composable
fun <T> SegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = DL.colors
    Row(
        modifier
            .fillMaxWidth()
            .background(c.backgroundSecondary, DLRadius.pillShape)
            .padding(DLSpacing.space4)
            .selectableGroup(),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = DLSize.tapMin - DLSpacing.space8)
                    .background(if (on) c.surfaceCard else androidx.compose.ui.graphics.Color.Transparent, DLRadius.pillShape)
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(value) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = DL.type.bodySmall, color = if (on) c.textPrimary else c.textSecondary)
            }
        }
    }
}

/** Star rating. [score] 0 means not rated; tapping a star reports 1..5. */
@Composable
fun RateStars(score: Int, onRate: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(modifier.selectableGroup()) {
        for (i in 1..5) {
            val filled = i <= score
            Box(
                Modifier
                    .size(DLSize.tapMin)
                    .clickable(enabled = enabled, role = Role.Button) { onRate(i) }
                    .semantics { contentDescription = "Rate $i out of 5" },
                contentAlignment = Alignment.Center,
            ) {
                DLIcon(DLIconName.Star, tint = if (filled) DL.colors.accentFavoriteText else DL.colors.textTertiary, size = DLSize.iconL, filled = filled)
            }
        }
    }
}

/** Today's remaining painting stories. */
@Composable
fun QuotaCard(remaining: Int, total: Int, modifier: Modifier = Modifier) {
    val c = DL.colors
    val out = remaining <= 0
    Column(
        modifier
            .fillMaxWidth()
            .background(if (out) c.warningSoft else c.accentSecondarySoft, DLRadius.mdShape)
            .padding(DLSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space8),
    ) {
        Text(
            if (out) "You've used all of today's painting stories." else "$remaining of $total painting stories left today",
            style = DL.type.bodySmall,
            color = c.textPrimary,
        )
        LinearProgressIndicator(
            progress = { if (total > 0) (total - remaining).coerceIn(0, total).toFloat() / total else 0f },
            modifier = Modifier.fillMaxWidth().height(DLSpacing.space8),
            color = if (out) c.warning else c.accentSecondaryText,
            trackColor = c.backgroundSecondary,
        )
    }
}

data class DLTab(val icon: DLIconName, val label: String)

/** Bottom bar with a round Create button in the middle, matching the iOS tab bar. */
@Composable
fun DLBottomBar(tabs: List<DLTab>, selected: Int, onSelect: (Int) -> Unit, onCreate: () -> Unit, modifier: Modifier = Modifier) {
    val c = DL.colors
    Column(modifier.background(c.backgroundElevated).navigationBarsPadding()) {
        HorizontalDivider(color = c.separator)
        Row(Modifier.fillMaxWidth().padding(horizontal = DLSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            tabs.forEachIndexed { index, tab ->
                if (index == (tabs.size + 1) / 2) CreateButton(onCreate)
                val on = index == selected
                val tint = if (on) c.navTint else c.textSecondary
                Column(
                    Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = DLSize.tapMin + DLSpacing.space8)
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(index) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    DLIcon(tab.icon, tint = tint, filled = on)
                    Text(tab.label, style = DL.type.caption, color = tint)
                }
            }
        }
    }
}

@Composable
private fun RowScope.CreateButton(onCreate: () -> Unit) {
    Box(Modifier.padding(horizontal = DLSpacing.space8), contentAlignment = Alignment.Center) {
        DLIconButton(
            icon = DLIconName.Plus,
            label = "Create a story",
            onClick = onCreate,
            tint = DL.colors.textOnAccent,
            container = DL.colors.accentPrimary,
            modifier = Modifier.size(DLSize.tapChild),
        )
    }
}
