package app.topgun.android.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS View/MainViews/HomeView.swift + ViewModel/HomeViewModel.swift + FilterView.swift */
@Composable
fun HomeScreen(onOpenAircraft: (Int) -> Unit, onOpenCountries: () -> Unit, onOpenManufacturers: () -> Unit, onOpenList: (String) -> Unit, onShowSignIn: () -> Unit, onShowPaywall: () -> Unit, onSetTabBarVisible: (Boolean) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("HomeScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
