package com.mehdin13.dreamland.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLRadius
import com.mehdin13.dreamland.designsystem.DLSize

/**
 * Portrait (3:4) cover art. While the cover is missing (still generating, or failed) a brand gradient stands in,
 * chosen from [seed] so each story keeps its own colors. Never pass the child's original painting here.
 */
@Composable
fun StoryCover(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    corner: Dp = DLRadius.lg,
) {
    val c = DL.colors
    val gradients = listOf(
        listOf(c.brandStorybookNavy, c.brandMoonLavender),
        listOf(c.brandSunriseCoral, c.brandStarlightGold),
        listOf(c.brandMidnightInk, c.brandStorybookNavy),
        listOf(c.brandMossGreen, c.brandPaperCream),
    )
    val colors = gradients[Math.floorMod(seed, gradients.size)]
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(DLSize.coverRatio)
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(colors))
            .semantics { contentDescription = "$title cover" },
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(DLSize.coverRatio),
            )
        } else {
            DLIcon(DLIconName.Sparkles, tint = c.onNight, size = DLSize.iconL)
        }
    }
}
