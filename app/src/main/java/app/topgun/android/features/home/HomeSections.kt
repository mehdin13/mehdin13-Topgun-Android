package app.topgun.android.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import app.topgun.android.R
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGHomeColors
import app.topgun.android.designsystem.TGHomeSize
import app.topgun.android.designsystem.TGHomeSpace
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AircraftListCard
import app.topgun.android.designsystem.components.CategoryTile
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.FeaturedSectionCard
import app.topgun.android.designsystem.components.GradientLoadMoreButton
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.android.designsystem.components.TGSearchField
import app.topgun.core.model.Aircraft

/** Where a "Welcome on board!" card goes. */
sealed interface FeatureTarget {
    data object Countries : FeatureTarget
    data object Manufacturers : FeatureTarget
    data class CustomList(val name: String) : FeatureTarget
}

/** iOS `FeaturedSectionItem` with its destination reduced to a [FeatureTarget]. */
data class FeatureCard(val titleRes: Int, val subtitleRes: Int, val icon: ImageVector, val target: FeatureTarget)

/** Same order and texts as `WelcomeSection.featuredSections`; gradients are assigned by position. */
val FeatureCards = listOf(
    FeatureCard(R.string.home_card_countries_title, R.string.home_card_countries_subtitle, Icons.Filled.Flag, FeatureTarget.Countries),
    FeatureCard(R.string.home_card_manufacturers_title, R.string.home_card_manufacturers_subtitle, Icons.Filled.Settings, FeatureTarget.Manufacturers),
    FeatureCard(R.string.home_card_selected_title, R.string.home_card_selected_subtitle, Icons.Filled.Star, FeatureTarget.CustomList("Topgun Selected")),
    FeatureCard(R.string.home_card_ww2_title, R.string.home_card_ww2_subtitle, Icons.Filled.Bolt, FeatureTarget.CustomList("Top WWII")),
    FeatureCard(R.string.home_card_coldwar_title, R.string.home_card_coldwar_subtitle, Icons.Filled.Security, FeatureTarget.CustomList("Cold War Icons")),
    FeatureCard(R.string.home_card_stealth_title, R.string.home_card_stealth_subtitle, Icons.Filled.VisibilityOff, FeatureTarget.CustomList("Stealth Dominance")),
    FeatureCard(R.string.home_card_carrier_title, R.string.home_card_carrier_subtitle, Icons.Filled.Waves, FeatureTarget.CustomList("Carrier Borne")),
    FeatureCard(R.string.home_card_helicopter_title, R.string.home_card_helicopter_subtitle, Icons.Filled.Shield, FeatureTarget.CustomList("Best Helicopter")),
)

/** The category chips of HomeView, with their tile colors and drawable icons. */
/** [name] is the value sent to the API; the chip shows its translation (see [optionLabel]). */
class HomeCategory(val name: String, val iconRes: Int, val tint: Color)

val HomeCategories = listOf(
    HomeCategory("Fighter", R.drawable.fighter_icon, TGHomeColors.systemRed),
    HomeCategory("Bomber", R.drawable.bomber_icon, TGHomeColors.systemBlue),
    HomeCategory("Reconnaissance", R.drawable.reconnaissance_icon, TGHomeColors.systemGreen),
    HomeCategory("Transport", R.drawable.transport_icon, TGHomeColors.lime),
    HomeCategory("Helicopters", R.drawable.helecopter_icon, TGHomeColors.coral),
    HomeCategory("Passenger", R.drawable.passenger_icon, TGHomeColors.systemGray),
    HomeCategory("Tanker", R.drawable.tanker_icon, TGHomeColors.ember),
    HomeCategory("UAVs", R.drawable.uav_icon, TGHomeColors.slate),
    HomeCategory("Trainer", R.drawable.trainer_icon, TGHomeColors.sky),
    HomeCategory("AWACS", R.drawable.awacs_icon, TGHomeColors.crimson),
    HomeCategory("Electronic Warfare", R.drawable.electronic_warfare_icon, TGHomeColors.systemPurple),
)

/** Logo and "Top Gun" (iOS `HeaderSection`): `topgun-logo8` in dark mode, `topgun-logo6` in light. */
@Composable
fun HomeHeader(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(top = TGHomeSpace.headerTop), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painterResource(if (TG.colors.isDark) R.drawable.topgun_logo8 else R.drawable.topgun_logo6),
            contentDescription = stringResource(R.string.home_logo_description),
            modifier = Modifier.size(TGHomeSize.logoWidth, TGHomeSize.logoHeight),
        )
        Text("Top Gun", style = TGText.logoTitle, color = TG.colors.textPrimary)
    }
}

/** Search bar plus the filter button (iOS `SearchAndFilterSection`). */
@Composable
fun SearchAndFilterRow(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onOpenFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().padding(top = TGSpace.l), verticalAlignment = Alignment.CenterVertically) {
        TGSearchField(searchText, onSearchTextChange, onSearch, Modifier.weight(1f))
        Box(
            Modifier.size(TGSize.tapMin).clickable(role = Role.Button, onClickLabel = stringResource(R.string.home_open_filters), onClick = onOpenFilter),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.home_filter), tint = TGHomeColors.systemGray) }
    }
}

/** "Welcome on board!" and the horizontally scrolling feature cards (iOS `WelcomeSection`). */
@Composable
fun WelcomeSection(onOpen: (FeatureTarget) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(TGHomeSpace.headerTop)) {
        Text(stringResource(R.string.home_welcome), style = TGText.title, color = TG.colors.textPrimary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(TGHomeSpace.section)) {
            items(FeatureCards.size) { i ->
                val card = FeatureCards[i]
                val gradients = TGHomeColors.featureGradients
                FeaturedSectionCard(stringResource(card.titleRes), stringResource(card.subtitleRes), card.icon, gradients[i % gradients.size], onClick = { onOpen(card.target) })
            }
        }
    }
}

/** "Category" title and the scrolling chips (iOS `CategoriesSection`). Tapping the selected chip again clears it. */
@Composable
fun CategoriesSection(selected: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.home_category), style = TGText.sectionTitle, color = TG.colors.textPrimary,
            modifier = Modifier.padding(top = TGHomeSpace.categoryTop),
        )
        LazyRow {
            items(HomeCategories.size) { i ->
                val c = HomeCategories[i]
                CategoryTile(optionLabel(c.name), c.iconRes, c.tint, selected = selected == c.name, onClick = { onSelect(c.name) })
            }
        }
    }
}

/** Callbacks of one aircraft row. */
class AircraftRowActions(
    val onOpen: (Aircraft) -> Unit,
    val onLockedTap: () -> Unit,
    val onToggleFavorite: (Aircraft) -> Unit,
    val onShare: (Aircraft) -> Unit,
)

/**
 * The part of Home below the categories (iOS `AircraftContentSection`): loading, error, empty, or
 * "Aircraft List" with rows and the Load More button.
 */
fun LazyListScope.aircraftContent(
    state: HomeState,
    isPremium: Boolean,
    actions: AircraftRowActions,
    onRetry: () -> Unit,
    onShowAll: () -> Unit,
    onLoadMore: () -> Unit,
) {
    when {
        state.isLoading -> item(key = "loading") { LoadingState(stringResource(R.string.home_loading_aircraft)) }
        state.error != null -> item(key = "error") {
            EmptyState(state.error.userMessage.ifEmpty { stringResource(R.string.home_error_generic) }, icon = Icons.Outlined.Flight, onRetry = onRetry)
        }
        state.items.isEmpty() -> item(key = "empty") {
            if (state.source is HomeSource.Search) {
                EmptyState(stringResource(R.string.home_no_results))
            } else {
                EmptyState(stringResource(R.string.home_no_aircraft), icon = Icons.Outlined.Flight, onRetry = onShowAll)
            }
        }
        else -> {
            item(key = "list-title") {
                Text(
                    stringResource(R.string.home_aircraft_list), style = TGText.sectionTitle, color = TG.colors.textPrimary,
                    modifier = Modifier.fillMaxWidth().padding(top = TGHomeSpace.listTitleTop, bottom = TGSpace.s),
                )
            }
            items(state.items, key = { it.id }) { aircraft ->
                AircraftRow(aircraft, isLocked = aircraft.isVip && !isPremium, actions)
            }
            if (state.hasMore) {
                item(key = "load-more") { GradientLoadMoreButton(isLoading = state.isPaginating, onClick = onLoadMore) }
            }
        }
    }
}

/** One list row: opens the details, or asks for sign-in / Pro when locked; long press opens favorite and share. */
@Composable
private fun AircraftRow(aircraft: Aircraft, isLocked: Boolean, actions: AircraftRowActions) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(bottom = TGSpace.xs)) {
        if (isLocked) {
            AircraftListCard(aircraft, isLocked = true, onClick = actions.onLockedTap)
        } else {
            AircraftListCard(
                aircraft, isLocked = false, onClick = { actions.onOpen(aircraft) },
                onLongClick = { menuOpen = true }, onLongClickLabel = stringResource(R.string.home_row_options),
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (aircraft.isFavorite) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.home_remove_favorite), color = TGBrand.red) },
                        leadingIcon = { Icon(Icons.Outlined.Favorite, contentDescription = null, tint = TGBrand.red) },
                        onClick = { menuOpen = false; actions.onToggleFavorite(aircraft) },
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.home_add_favorite)) },
                        leadingIcon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                        onClick = { menuOpen = false; actions.onToggleFavorite(aircraft) },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_share)) },
                    leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                    onClick = { menuOpen = false; actions.onShare(aircraft) },
                )
            }
        }
    }
}
