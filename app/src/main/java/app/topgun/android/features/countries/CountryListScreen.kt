package app.topgun.android.features.countries

import androidx.compose.ui.res.stringResource
import app.topgun.android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Load
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.FeaturedCard
import app.topgun.android.designsystem.components.GeoIconButton
import app.topgun.android.designsystem.components.GeoTopBar
import app.topgun.android.designsystem.components.LoadingState

/**
 * "Top Countries" (iOS CountryListView): 3-column grid of country cards, paginated, with search by name and a
 * grid / map / globe switch (the map and globe are iOS MapView / GlobeView, here fed with every country).
 */
@Composable
fun CountryListScreen(onBack: () -> Unit, onOpenCountry: (Int) -> Unit) {
    val vm = containerViewModel("countries") { CountryListViewModel(it.api) }
    val state by vm.state.collectAsStateWithLifecycle()
    CountryListContent(
        state = state, onBack = onBack, onOpenCountry = onOpenCountry,
        onQueryChange = vm::onQueryChange, onLoadMore = vm::loadMore, onRetry = vm::reload,
        onModeChange = vm::setMode, onRetryMap = vm::retryMap,
    )
}

@Composable
fun CountryListContent(
    state: CountryListState,
    onBack: () -> Unit,
    onOpenCountry: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onModeChange: (CountryViewMode) -> Unit,
    onRetryMap: () -> Unit,
) {
    val c = TG.colors
    Column(Modifier.fillMaxSize().background(c.background)) {
        GeoTopBar(stringResource(R.string.countries_title), onBack) {
            ModeButton(Icons.Filled.ViewModule, stringResource(R.string.countries_show_grid), state.mode == CountryViewMode.Grid) { onModeChange(CountryViewMode.Grid) }
            ModeButton(Icons.Filled.Map, stringResource(R.string.countries_show_map), state.mode == CountryViewMode.Map) { onModeChange(CountryViewMode.Map) }
            ModeButton(Icons.Filled.Public, stringResource(R.string.countries_show_globe), state.mode == CountryViewMode.Globe) { onModeChange(CountryViewMode.Globe) }
        }
        OutlinedTextField(
            value = state.query, onValueChange = onQueryChange, singleLine = true,
            placeholder = { Text(stringResource(R.string.countries_search), style = TGText.body) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            textStyle = TGText.sailecRegular(16),
            shape = TGRadius.button,
            modifier = Modifier.fillMaxWidth().padding(horizontal = TGSpace.l, vertical = TGSpace.s),
        )
        when (state.mode) {
            CountryViewMode.Grid -> GridBody(state, onOpenCountry, onLoadMore, onRetry)
            else -> Box(Modifier.weight(1f).fillMaxWidth()) {
                MapBody(state, state.mode == CountryViewMode.Globe, onOpenCountry, onRetryMap)
            }
        }
    }
}

@Composable
private fun ModeButton(icon: ImageVector, description: String, selected: Boolean, onClick: () -> Unit) {
    GeoIconButton(icon, description, onClick, tint = if (selected) TG.colors.accent else TG.colors.textSecondary)
}

@Composable
private fun GridBody(state: CountryListState, onOpenCountry: (Int) -> Unit, onLoadMore: () -> Unit, onRetry: () -> Unit) {
    // Defensive: repeated ids would crash the lazy grid (the view model already drops them).
    val grid = remember(state.countries) { state.countries.distinctBy { it.id } }
    when (val initial = state.initial) {
        Load.Loading -> LoadingState(stringResource(R.string.countries_loading))
        is Load.Failed -> ErrorState(initial.error.userMessage, onRetry = onRetry)
        is Load.Loaded -> if (state.countries.isEmpty()) {
            EmptyState(stringResource(R.string.countries_none_found))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(TGGeo.featuredColumns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = TGSpace.l, end = TGSpace.l, bottom = TGSpace.xl),
                horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
                verticalArrangement = Arrangement.spacedBy(TGSpace.m),
            ) {
                itemsIndexed(grid, key = { _, country -> country.id }) { index, country ->
                    FeaturedCard(
                        name = country.name, iconPath = country.flagIcon ?: country.icon, coverPath = country.cover,
                        code = country.code, onClick = { onOpenCountry(country.id) },
                    )
                    if (index == grid.lastIndex && state.hasMore) {
                        LaunchedEffect(grid.size) { onLoadMore() }
                    }
                }
                if (state.isLoadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(Modifier.fillMaxWidth().padding(TGSpace.l), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TG.colors.accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapBody(state: CountryListState, globe: Boolean, onOpenCountry: (Int) -> Unit, onRetryMap: () -> Unit) {
    when (val all = state.mapCountries) {
        null, Load.Loading -> LoadingState(stringResource(R.string.countries_loading))
        is Load.Failed -> ErrorState(all.error.userMessage, onRetry = onRetryMap)
        is Load.Loaded -> {
            val q = state.query.trim()
            val shown = if (q.isEmpty()) all.value else all.value.filter { it.name.contains(q, ignoreCase = true) }
            if (shown.none { it.latitude != null && it.longitude != null }) EmptyState(stringResource(R.string.countries_none_with_location)) else
            CountryMapPanel(shown, globe, onOpenCountry, Modifier.fillMaxSize())
        }
    }
}
