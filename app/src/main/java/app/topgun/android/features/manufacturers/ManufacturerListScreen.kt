package app.topgun.android.features.manufacturers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.R
import app.topgun.android.data.Load
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.GeoFlag
import app.topgun.android.designsystem.components.GeoTopBar
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.android.designsystem.components.TGImage
import app.topgun.core.model.Manufacturer

/** "Top Manufacturers" (iOS ManufacturerListView): one big card per manufacturer (two columns on wide screens). */
@Composable
fun ManufacturerListScreen(onBack: () -> Unit, onOpenManufacturer: (Int) -> Unit) {
    val vm = containerViewModel("manufacturers") { ManufacturerListViewModel(it.api) }
    val state by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(TG.colors.background)) {
        GeoTopBar("Manufacturers", onBack)
        when (val s = state) {
            Load.Loading -> LoadingState("Loading manufacturers...")
            is Load.Failed -> ErrorState("Failed to load manufacturers", onRetry = vm::load)
            is Load.Loaded -> if (s.value.isEmpty()) {
                EmptyState("No manufacturers available", icon = Icons.Filled.Business, onRetry = vm::load)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(TGGeo.wideCardWidth),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = TGSpace.l, end = TGSpace.l, bottom = TGSpace.xl),
                    horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
                    verticalArrangement = Arrangement.spacedBy(TGSpace.l),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(Modifier.padding(top = TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                            Text("Top Manufacturers", style = TGText.rounded(28, FontWeight.Bold), color = TG.colors.textPrimary)
                            Text("Leading global aerospace brands", style = TGText.sailecRegular(14), color = TG.colors.textSecondary)
                        }
                    }
                    items(s.value.distinctBy { it.id }, key = { it.id }) { m -> ManufacturerCard(m) { onOpenManufacturer(m.id) } }
                }
            }
        }
    }
}

/** iOS ManufacturerCard: cover with a white logo badge, then name, country (flag) and aircraft count. */
@Composable
fun ManufacturerCard(manufacturer: Manufacturer, onClick: () -> Unit) {
    val c = TG.colors
    Column(Modifier.fillMaxWidth().clip(TGRadius.card).clickable(role = Role.Button, onClick = onClick)) {
        Box(Modifier.fillMaxWidth().height(TGGeo.manufacturerCover)) {
            if (manufacturer.cover.isNullOrBlank()) {
                Image(painterResource(R.drawable.topgun_cover2), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                TGImage(manufacturer.cover, null, Modifier.fillMaxSize())
            }
            Box(
                Modifier.align(Alignment.BottomStart).padding(TGSpace.m)
                    .clip(TGGeo.shape12).background(Color.White).padding(TGSpace.s),
            ) {
                if (manufacturer.logo.isNullOrBlank()) {
                    Image(painterResource(R.drawable.topgun_logo2), "${manufacturer.name} logo", Modifier.size(TGGeo.manufacturerBadge), contentScale = ContentScale.Fit)
                } else {
                    TGImage(manufacturer.logo, "${manufacturer.name} logo", Modifier.size(TGGeo.manufacturerBadge), ContentScale.Fit)
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().background(c.card).padding(TGSpace.m),
            verticalArrangement = Arrangement.spacedBy(TGSpace.s),
        ) {
            Text(manufacturer.name, style = TGText.sailecBold(16), color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.xs + TGSpace.xs / 2)) {
                val country = manufacturer.country
                if (country?.flagIcon.isNullOrBlank()) {
                    Icon(Icons.Filled.Flag, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(TGGeo.manufacturerFlagHeight))
                } else {
                    GeoFlag(country?.flagIcon, country?.code, null, Modifier.size(TGGeo.manufacturerFlagWidth, TGGeo.manufacturerFlagHeight))
                }
                Text(country?.name ?: "Unknown", style = TGText.sailecRegular(12), color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("${manufacturer.aircraftList.size} Aircraft", style = TGText.sailecBold(12), color = TGBrand.blue)
        }
    }
}
