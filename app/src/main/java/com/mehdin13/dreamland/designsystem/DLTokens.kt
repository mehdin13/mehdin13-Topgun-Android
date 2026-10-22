package com.mehdin13.dreamland.designsystem

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/** Spacing, radius, size and layout tokens from tokens.json. Android dp = iOS pt (1:1). Never hardcode these values. */
object DLSpacing {
    val space2 = 2.dp
    val space4 = 4.dp
    val space8 = 8.dp
    val space12 = 12.dp
    val space16 = 16.dp
    val space20 = 20.dp
    val space24 = 24.dp
    val space32 = 32.dp
    val space40 = 40.dp
    val space48 = 48.dp
}

object DLRadius {
    val xs = 6.dp
    val sm = 10.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val pill = 999.dp

    val xsShape = RoundedCornerShape(xs)
    val smShape = RoundedCornerShape(sm)
    val mdShape = RoundedCornerShape(md)
    val lgShape = RoundedCornerShape(lg)
    val xlShape = RoundedCornerShape(xl)
    val pillShape = RoundedCornerShape(pill)
}

object DLSize {
    /** Minimum touch target for every control. */
    val tapMin = 44.dp
    /** Touch target for child-facing primary controls (play, page turn, record). */
    val tapChild = 56.dp
    val controlH = 52.dp
    val iconS = 17.dp
    val iconM = 22.dp
    val iconL = 28.dp
    /** Cover art aspect ratio (width / height), 3:4 portrait. */
    const val coverRatio = 0.75f
}

object DLLayout {
    val screenMarginCompact = 16.dp
    val screenMarginCompactLarge = 20.dp
    val screenMarginRegular = 32.dp
    val screenMarginRegularWide = 40.dp
    val gridGutterCompact = 16.dp
    val gridGutterRegular = 24.dp
    val gridTileMinCompact = 150.dp
    val gridTileMinRegular = 180.dp
    val gridTileMax = 260.dp
    val readableWidth = 672.dp
}

/** Motion durations in ms (tokens.json `duration`). Reduce motion replaces movement with a fade. */
object DLMotion {
    const val instant = 100
    const val fade = 150
    const val quick = 200
    const val gentle = 350
    const val calm = 500
    const val reveal = 900
    const val loop = 2400
}
