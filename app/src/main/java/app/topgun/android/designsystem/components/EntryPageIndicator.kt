package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGEntry
import app.topgun.android.designsystem.TGEntryColors

/** Row of small dots (iOS `CustomIndicatorView`). One announcement for TalkBack instead of one per dot. */
@Composable
fun EntryPageIndicator(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeTint: Color = if (TG.colors.isDark) TG.colors.accent else TGEntryColors.black,
    inactiveTint: Color = TGBrand.gray.copy(alpha = 0.5f),
) {
    Row(
        modifier.clearAndSetSemantics { contentDescription = "Page ${currentPage + 1} of $totalPages" },
        horizontalArrangement = Arrangement.spacedBy(TGEntry.indicatorSpacing),
    ) {
        repeat(totalPages) { index ->
            androidx.compose.foundation.layout.Box(
                Modifier.size(TGEntry.indicatorDot).background(if (index == currentPage) activeTint else inactiveTint, CircleShape),
            )
        }
    }
}
