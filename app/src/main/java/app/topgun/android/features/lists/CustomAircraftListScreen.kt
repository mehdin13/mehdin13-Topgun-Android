package app.topgun.android.features.lists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS View/SubViews/CustomAircraftGridView.swift (Topgun Selected, Top WWII, Cold War Icons, ...) */
@Composable
fun CustomAircraftListScreen(name: String, onBack: () -> Unit, onOpenAircraft: (Int) -> Unit, onShowSignIn: () -> Unit, onShowPaywall: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("CustomAircraftListScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
