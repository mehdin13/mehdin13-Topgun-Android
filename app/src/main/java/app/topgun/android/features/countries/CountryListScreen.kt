package app.topgun.android.features.countries

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS View/SubViews/CountryListView.swift + MainViews/CountryView.swift (+ map/globe) */
@Composable
fun CountryListScreen(onBack: () -> Unit, onOpenCountry: (Int) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("CountryListScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
