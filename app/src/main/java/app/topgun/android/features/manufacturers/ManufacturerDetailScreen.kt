package app.topgun.android.features.manufacturers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.R
import app.topgun.android.data.Load
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.EquipmentCard
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.GeoCover
import app.topgun.android.designsystem.components.GeoOverlayButton
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.android.designsystem.components.TGImage
import app.topgun.android.features.countries.CategoryFilter
import app.topgun.core.model.Manufacturer

/**
 * Manufacturer page (iOS ManufacturerView): cover with round logo, name, Wikipedia / website buttons,
 * description and a filterable 2-row equipment grid.
 */
@Composable
fun ManufacturerDetailScreen(id: Int, onBack: () -> Unit, onOpenAircraft: (Int) -> Unit) {
    val vm = containerViewModel("manufacturer-$id") { ManufacturerDetailViewModel(it.api, id) }
    val state by vm.state.collectAsStateWithLifecycle()
    val platform = LocalAppContainer.current.platform
    Box(Modifier.fillMaxSize().background(TG.colors.background)) {
        when (val s = state) {
            Load.Loading -> LoadingState("Loading manufacturer...", Modifier.align(Alignment.Center))
            is Load.Failed -> ErrorState(s.error.userMessage, Modifier.align(Alignment.Center), onRetry = vm::load)
            is Load.Loaded -> ManufacturerDetailContent(s.value, onOpenAircraft, platform::openUrl)
        }
        GeoOverlayButton(
            Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack,
            Modifier.statusBarsPadding().padding(start = TGSpace.s, top = TGSpace.xs),
        )
    }
}

@Composable
fun ManufacturerDetailContent(manufacturer: Manufacturer, onOpenAircraft: (Int) -> Unit, onOpenUrl: (String) -> Unit) {
    val c = TG.colors
    var category by remember { mutableStateOf("All") }
    val categories = remember(manufacturer) {
        listOf("All") + manufacturer.aircraftList.mapNotNull { it.category }.filter { it.isNotEmpty() }.toSet().sorted()
    }
    val equipment = remember(manufacturer, category) {
        if (category == "All") manufacturer.aircraftList else manufacturer.aircraftList.filter { it.category == category }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth()) {
            GeoCover(manufacturer.cover, null, Modifier.fillMaxWidth().height(TGGeo.coverHeight))
            Box(
                Modifier.align(Alignment.BottomStart).offset(x = TGSpace.l, y = TGGeo.coverLogo / 2)
                    .size(TGGeo.coverLogo).clip(CircleShape).background(Color.White)
                    .border(TGGeo.coverLogoBorder, Color.White, CircleShape),
            ) {
                if (manufacturer.logo.isNullOrBlank()) {
                    androidx.compose.foundation.Image(
                        painterResource(R.drawable.company_sample), "${manufacturer.name} logo",
                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                    )
                } else {
                    TGImage(manufacturer.logo, "${manufacturer.name} logo", Modifier.fillMaxSize(), ContentScale.Fit)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = TGSpace.xxl + TGSpace.s, start = TGSpace.l, end = TGSpace.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                manufacturer.name.ifEmpty { "Manufacturer" }, style = TGText.title, color = c.textPrimary,
                modifier = Modifier.weight(1f),
            )
            manufacturer.wikipediaLink?.takeIf { it.isNotBlank() }?.let { link ->
                LinkCircle("Wikipedia", TGBrand.blue, { onOpenUrl(link) }) {
                    Text("W", style = TGText.sailecBold(16), color = TGBrand.blue)
                }
            }
            manufacturer.websiteLink?.takeIf { it.isNotBlank() }?.let { link ->
                LinkCircle("Website", TGBrand.red, { onOpenUrl(link) }) {
                    Icon(Icons.Filled.Public, contentDescription = null, tint = TGBrand.red)
                }
            }
        }
        Text(
            manufacturer.description?.takeIf { it.isNotBlank() } ?: "No description available",
            style = TGText.sailecRegular(16), color = c.textSecondary,
            modifier = Modifier.padding(start = TGSpace.l, end = TGSpace.l, top = TGSpace.s),
        )
        Row(
            Modifier.fillMaxWidth().padding(start = TGSpace.l, end = TGSpace.l, top = TGSpace.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Equipment:", style = TGText.sailecBold(20), color = c.textPrimary)
            CategoryFilter(category, categories) { category = it }
        }
        if (equipment.isEmpty()) {
            Text("No aircraft", style = TGText.body, color = c.textSecondary, modifier = Modifier.padding(TGSpace.l))
        } else {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(TGGeo.manufacturerEquipmentHeight),
                contentPadding = PaddingValues(horizontal = TGSpace.l, vertical = TGSpace.xl - TGSpace.xs),
                horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
                verticalArrangement = Arrangement.spacedBy(TGSpace.l),
            ) {
                items(equipment, key = { it.id }) { plane ->
                    // The manufacturer's aircraft rows carry no thumbnail (docs/BACKEND_GAPS.md), so the placeholder shows.
                    EquipmentCard(plane.name ?: "Unknown", null, isLocked = false, onClick = { onOpenAircraft(plane.id) })
                }
            }
        }
        Box(Modifier.height(TGSpace.xxl))
    }
}

/** Round tinted link button (iOS `w.circle.fill` / `globe` on a blue-tinted circle), 48dp touch target. */
@Composable
private fun LinkCircle(description: String, tint: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(TGSize.tapMin).clip(CircleShape).background(tint.copy(alpha = TGGeo.tint))
            .clickable(role = Role.Button, onClickLabel = "Open $description", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
