package app.topgun.android.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText

/**
 * Instagram-style page indicator: never more than [TGDetailSize.dotWindow] dots are visible, however many pages
 * there are. The selected dot is larger and in the accent colour, the dots at the edge of the window shrink (when
 * more pages continue beyond that edge), and the window slides smoothly with [position] (pass
 * `pagerState.currentPage + pagerState.currentPageOffsetFraction` for a swipe-synchronised animation).
 */
@Composable
fun SlidingDotsIndicator(
    pageCount: Int,
    position: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = TG.colors.accent,
    inactiveColor: Color = TGBrand.gray,
) {
    if (pageCount <= 1) return
    val window = TGDetailSize.dotWindow
    val visible = minOf(pageCount, window)
    val slotDp = TGDetailSize.dotSize + TGDetailSize.dotGap
    val selected = position.coerceIn(0f, (pageCount - 1).toFloat())
    // Left edge of the window, in dot slots; keeps the selected dot in the middle where there is room.
    val maxStart = (pageCount - window).coerceAtLeast(0).toFloat()
    val start = (selected - (window - 1) / 2f).coerceIn(0f, maxStart)
    val leftWeight = start.coerceIn(0f, 1f)
    val rightWeight = (maxStart - start).coerceIn(0f, 1f)
    val description = "Photo ${(selected + 0.5f).toInt() + 1} of $pageCount"

    Canvas(
        modifier
            .size(slotDp * visible, TGDetailSize.dotSize * TGDetailSize.dotSelectedScale)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        val slot = slotDp.toPx()
        val base = TGDetailSize.dotSize.toPx() / 2f
        val first = (start.toInt() - 1).coerceAtLeast(0)
        val last = (start.toInt() + window + 1).coerceAtMost(pageCount - 1)
        for (i in first..last) {
            val fromLeft = i - start
            val fromRight = (start + window - 1) - i
            val edge = minOf(edgeScale(fromLeft, leftWeight), edgeScale(fromRight, rightWeight))
            val closeness = (1f - kotlin.math.abs(i - selected)).coerceIn(0f, 1f)
            val scale = edge * (1f + (TGDetailSize.dotSelectedScale - 1f) * closeness)
            val x = (fromLeft + 0.5f) * slot
            if (x < -slot || x > size.width + slot) continue
            drawCircle(lerp(inactiveColor, activeColor, closeness), radius = base * scale, center = Offset(x, size.height / 2f))
        }
    }
}

/** Scale (0..1) of a dot that is [distance] slots inside the window edge; [weight] fades the effect in as the window starts to slide. */
private fun edgeScale(distance: Float, weight: Float): Float {
    val shrunk = when {
        distance >= 1f -> 1f
        distance <= -1f -> 0f
        distance >= 0f -> TGDetailSize.dotEdgeScale + (1f - TGDetailSize.dotEdgeScale) * distance
        else -> TGDetailSize.dotEdgeScale * (1f + distance)
    }
    return 1f + (shrunk - 1f) * weight
}

/** Small "3 / 24" pill for the full-screen gallery. [current] is zero-based. */
@Composable
fun PageCounterPill(current: Int, total: Int, modifier: Modifier = Modifier) {
    if (total <= 1) return
    Text(
        "${current + 1} / $total",
        style = TGText.sailecMedium(13),
        color = TGDetailColors.onPhoto,
        modifier = modifier
            .background(TGDetailColors.photoBlack.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = TGSpace.m, vertical = TGSpace.xs)
            .clearAndSetSemantics { contentDescription = "Photo ${current + 1} of $total" },
    )
}
