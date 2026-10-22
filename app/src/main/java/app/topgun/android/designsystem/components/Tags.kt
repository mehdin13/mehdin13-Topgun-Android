package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGText

/** Small colored tag used on aircraft rows: [tint] text and icon on a 10% tint background (iOS red_color_trans etc.). */
@Composable
fun TagChip(text: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.background(tint.copy(alpha = 0.12f), TGRadius.chip).padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(end = 3.dp).size(10.dp))
        Text(text, style = TGText.sailecRegular(10), color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
