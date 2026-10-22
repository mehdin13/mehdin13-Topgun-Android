package app.topgun.android.features.countries

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Load
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGGeoColor
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.EquipmentCard
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.GeoCover
import app.topgun.android.designsystem.components.GeoFlag
import app.topgun.android.designsystem.components.GeoLinkButton
import app.topgun.android.designsystem.components.GeoOverlayButton
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.android.designsystem.components.TGImage
import app.topgun.core.model.Country
import app.topgun.core.model.ManufacturerSimple

/** Category filter of the equipment grid (iOS CountryView `categories`). */
private val equipmentCategories = listOf("All", "Fighter", "Attack", "Bomber", "Spy", "Transport", "Reconnaissance", "Tanker", "Helicopters")

/**
 * Country page (iOS CountryView): cover with a round flag, name, capital, description, highlights, filterable
 * 2-row equipment grid and the country's manufacturers.
 */
@Composable
fun CountryDetailScreen(id: Int, onBack: () -> Unit, onOpenAircraft: (Int) -> Unit, onOpenManufacturer: (Int) -> Unit) {
    val vm = containerViewModel("country-$id") { CountryDetailViewModel(it.api, id) }
    val state by vm.state.collectAsStateWithLifecycle()
    val platform = LocalAppContainer.current.platform
    Box(Modifier.fillMaxSize().background(TG.colors.background)) {
        when (val s = state) {
            Load.Loading -> LoadingState("Loading country...", Modifier.align(Alignment.Center))
            is Load.Failed -> ErrorState(s.error.userMessage, Modifier.align(Alignment.Center), onRetry = vm::load)
            is Load.Loaded -> CountryDetailContent(s.value, onOpenAircraft, onOpenManufacturer, platform::openUrl)
        }
        GeoOverlayButton(
            Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack,
            Modifier.statusBarsPadding().padding(start = TGSpace.s, top = TGSpace.xs),
        )
    }
}

@Composable
fun CountryDetailContent(
    country: Country,
    onOpenAircraft: (Int) -> Unit,
    onOpenManufacturer: (Int) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val c = TG.colors
    var category by remember { mutableStateOf("All") }
    val planes = remember(country, category) {
        if (category == "All") country.aircraftList else country.aircraftList.filter { it.category == category }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CountryCover(country)

        Column(
            Modifier.padding(start = TGGeo.detailHorizontal, end = TGGeo.detailHorizontal, top = TGGeo.detailTopInset),
            verticalArrangement = Arrangement.spacedBy(TGSpace.s),
        ) {
            Text(country.name.ifEmpty { "--" }, style = TGText.sailecMedium(29), color = c.textPrimary)
            country.capital?.takeIf { it.isNotEmpty() }?.let {
                Text(it, style = TGText.sailecMedium(16), color = c.textPrimary)
            }
            Text(
                country.description.orEmpty(), style = TGText.sailecRegular(15), color = c.textPrimary,
                modifier = Modifier.padding(vertical = TGSpace.l),
            )
        }

        Column(
            Modifier.padding(horizontal = TGGeo.detailHorizontal, vertical = TGSpace.l),
            verticalArrangement = Arrangement.spacedBy(TGSpace.l - TGSpace.xs / 4),
        ) {
            Text("HIGHLIGHTS", style = TGText.sailecMedium(12), color = c.textSecondary)
            HighlightRow("Air Force Founded:", country.airForceFounded?.takeIf { it.isNotEmpty() } ?: "--", TGBrand.red)
            HighlightRow("Number of Aircraft:", country.aircraftCount.toString(), TGBrand.blue)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                Icon(Icons.Filled.AirplanemodeActive, contentDescription = null, tint = TGBrand.green)
                Text("Air Force Wikipedia:", style = TGText.sailecRegular(16), color = c.textPrimary)
            }
            country.airForceLink?.takeIf { it.isNotBlank() }?.let { link ->
                Row(horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                    GeoLinkButton(country.name.ifEmpty { "Link" }, Icons.Filled.Public, TGBrand.green, { onOpenUrl(link) }, Modifier.weight(1f, fill = false))
                    GeoLinkButton("Air Force", Icons.Filled.Flight, TGGeoColor.purple, { onOpenUrl(link) })
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = TGSpace.l, vertical = TGSpace.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Equipment:", style = TGText.sailecBold(20), color = c.textPrimary)
            CategoryFilter(category, equipmentCategories) { category = it }
        }
        if (planes.isEmpty()) {
            Text(
                "No aircraft", style = TGText.body, color = c.textSecondary,
                modifier = Modifier.padding(horizontal = TGSpace.l, vertical = TGSpace.xl),
            )
        } else {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(TGGeo.countryEquipmentHeight),
                contentPadding = PaddingValues(horizontal = TGSpace.l, vertical = TGSpace.xl - TGSpace.xs),
                horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
                verticalArrangement = Arrangement.spacedBy(TGSpace.l),
            ) {
                items(planes, key = { it.id }) { plane ->
                    // iOS locks every card for free users here (a placeholder until the API sends is_vip); the
                    // row has no is_vip field, so Android opens the detail and lets it handle the Pro lock.
                    EquipmentCard(plane.name, plane.thumbnail, isLocked = false, onClick = { onOpenAircraft(plane.id) })
                }
            }
        }

        if (country.manufacturers.isNotEmpty()) {
            Text(
                "Manufacturers:", style = TGText.sailecBold(20), color = c.textPrimary,
                modifier = Modifier.padding(horizontal = TGSpace.l, vertical = TGSpace.s),
            )
            LazyRow(
                contentPadding = PaddingValues(start = TGSpace.l, end = TGSpace.l, top = TGSpace.m, bottom = TGSpace.xxl),
                horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
            ) {
                items(country.manufacturers, key = { it.id }) { m -> ManufacturerWideCard(m) { onOpenManufacturer(m.id) } }
            }
        }
        Box(Modifier.height(TGSpace.xxl))
    }
}

/** Cover photo with the flag (alternating with the country icon every 5 s, like iOS) half over its bottom edge. */
@Composable
private fun CountryCover(country: Country) {
    val images = remember(country) { listOfNotNull(country.icon, country.flagIcon).filter { it.isNotBlank() } }
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(images) {
        if (images.size > 1) while (true) {
            kotlinx.coroutines.delay(SWITCH_MS)
            index = (index + 1) % images.size
        }
    }
    Box(Modifier.fillMaxWidth()) {
        GeoCover(country.cover, null, Modifier.fillMaxWidth().height(TGGeo.coverHeight))
        Box(
            Modifier.align(Alignment.BottomStart)
                .offset(x = TGSpace.l, y = TGGeo.coverLogo / 2)
                .size(TGGeo.coverLogo).clip(CircleShape)
                .border(TGGeo.coverLogoBorder, Color.White, CircleShape),
        ) {
            val path = images.getOrNull(index % maxOf(images.size, 1))
            GeoFlag(path, country.code, "${country.name} flag", Modifier.size(TGGeo.coverLogo), ContentScale.Crop)
        }
    }
}

private const val SWITCH_MS = 5000L

@Composable
private fun HighlightRow(title: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        Icon(Icons.Filled.AirplanemodeActive, contentDescription = null, tint = tint)
        Text(title, style = TGText.sailecRegular(16), color = TG.colors.textPrimary)
        Text(value, style = TGText.sailecBold(16), color = TG.colors.textPrimary)
    }
}

/** iOS "Filter" menu: blue pill that opens the category list with a check on the selected one. */
@Composable
fun CategoryFilter(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.heightIn(min = TGSize.tapMin).clip(TGGeo.shape12)
                .background(TGBrand.blue.copy(alpha = TGGeo.glass))
                .clickable(role = Role.Button) { open = true }
                .padding(horizontal = TGSpace.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.xs),
        ) {
            Text("Filter", style = TGText.sailecMedium(14), color = TGBrand.blue)
            Icon(Icons.Filled.FilterList, contentDescription = "Filter by category", tint = TGBrand.blue)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = TGText.sailecRegular(16)) },
                    trailingIcon = { if (option == selected) Icon(Icons.Filled.Check, contentDescription = "Selected") },
                    onClick = { onSelect(option); open = false },
                )
            }
        }
    }
}

/** iOS ManufacturerWideCard: 320 x 140 cover with a dark gradient, round logo and name. */
@Composable
fun ManufacturerWideCard(manufacturer: ManufacturerSimple, onClick: () -> Unit) {
    Box(
        Modifier.size(TGGeo.wideCardWidth, TGGeo.wideCardHeight).clip(TGGeo.shape20)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(TGBrand.blue.copy(alpha = TGGeo.scrim), TGGeoColor.purple.copy(alpha = TGGeo.scrim))),
            ),
        )
        if (!manufacturer.cover.isNullOrBlank()) TGImage(manufacturer.cover, null, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))),
        )
        Row(
            Modifier.align(Alignment.BottomStart).padding(TGSpace.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
        ) {
            if (!manufacturer.logo.isNullOrBlank()) {
                Box(
                    Modifier.clip(CircleShape).background(Color.White.copy(alpha = TGGeo.tint)).padding(TGSpace.s),
                ) {
                    TGImage(manufacturer.logo, "${manufacturer.name} logo", Modifier.size(TGGeo.wideCardLogo), ContentScale.Fit)
                }
            }
            Text(
                manufacturer.name, style = TGText.sailecBold(17), color = Color.White,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
