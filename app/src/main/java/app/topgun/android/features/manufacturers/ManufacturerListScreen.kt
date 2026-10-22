package app.topgun.android.features.manufacturers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS View/SubViews/ManufacturerListView.swift + ManufacturerKeyListView */
@Composable
fun ManufacturerListScreen(onBack: () -> Unit, onOpenManufacturer: (Int) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("ManufacturerListScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
