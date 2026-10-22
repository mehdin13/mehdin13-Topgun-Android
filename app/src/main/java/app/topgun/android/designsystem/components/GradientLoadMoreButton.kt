package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGHomeColors
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText

/** "Load More" pagination button (iOS HomeView): blue to purple gradient, spinner and "Loading more..." while [isLoading]. */
@Composable
fun GradientLoadMoreButton(isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = TGSpace.l, vertical = TGSpace.s)
            .heightIn(min = TGSize.tapMin)
            .clip(TGRadius.button)
            .background(Brush.horizontalGradient(listOf(TGBrand.blue, TGHomeColors.systemPurple)))
            .clickable(enabled = !isLoading, role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(TGSpace.l), color = TGHomeColors.white, strokeWidth = TGSpace.xs / 2)
            Text("Loading more...", style = TGText.sailecRegular(14), color = TGHomeColors.white, modifier = Modifier.padding(start = TGSpace.s))
        } else {
            Text("Load More", style = TGText.sailecMedium(14), color = TGHomeColors.white)
        }
    }
}
