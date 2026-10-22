package app.topgun.android.features.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS View/DetailsView.swift + SubViews/DetailsView+Components + AircraftHeaderView + Aircraft3DView + GalleryView + PDFViewer + VideoPlayerView + CommentsListView */
@Composable
fun AircraftDetailScreen(id: Int, onBack: () -> Unit, onOpenAircraft: (Int) -> Unit, onOpenCountry: (Int) -> Unit, onOpenManufacturer: (Int) -> Unit, onShowSignIn: () -> Unit, onShowPaywall: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("AircraftDetailScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
