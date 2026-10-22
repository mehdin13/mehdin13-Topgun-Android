package app.topgun.android.features.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS View/MainViews/FavoriteView.swift */
@Composable
fun FavoritesScreen(onOpenAircraft: (Int) -> Unit, onShowSignIn: () -> Unit, onShowPaywall: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("FavoritesScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
