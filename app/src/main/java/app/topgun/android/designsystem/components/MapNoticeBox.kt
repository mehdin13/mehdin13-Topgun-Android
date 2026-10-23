package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText

/** Message shown instead of / over a Google map that cannot load (missing key, bad key restrictions, no network). */
@Composable
fun MapNoticeBox(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.background(TG.colors.background).padding(TGSpace.xl),
        verticalArrangement = Arrangement.spacedBy(TGSpace.m, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.Map, contentDescription = null, tint = TG.colors.textSecondary)
        Text(
            message, style = TGText.sailecMedium(16), color = TG.colors.textPrimary,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
    }
}
