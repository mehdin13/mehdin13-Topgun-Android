package app.topgun.android.features.lists

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.R
import app.topgun.android.data.Load
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.GeoTopBar
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.android.designsystem.components.LockScrim
import app.topgun.android.designsystem.components.TGImage
import app.topgun.core.model.Aircraft

/**
 * Curated list ("Topgun Selected", "Top WWII", ...; iOS CustomAircraftGridView): adaptive grid of aircraft cards.
 * A Pro-only aircraft is locked for free users; tapping it opens the paywall (or sign-in when signed out).
 */
@Composable
fun CustomAircraftListScreen(name: String, onBack: () -> Unit, onOpenAircraft: (Int) -> Unit, onShowSignIn: () -> Unit, onShowPaywall: () -> Unit) {
    val container = LocalAppContainer.current
    val vm = containerViewModel("list-$name") { CustomAircraftListViewModel(it.api, name) }
    val state by vm.state.collectAsStateWithLifecycle()
    val user by container.user.state.collectAsStateWithLifecycle()
    val title = (state as? Load.Loaded)?.value?.name?.takeIf { it.isNotEmpty() } ?: name
    Column(Modifier.fillMaxSize().background(TG.colors.background)) {
        GeoTopBar(title, onBack)
        when (val s = state) {
            Load.Loading -> LoadingState(stringResource(R.string.lists_loading_aircraft))
            is Load.Failed -> ErrorState(s.error.userMessage, onRetry = vm::load)
            is Load.Loaded -> if (s.value.aircraftList.isEmpty()) {
                EmptyState(stringResource(R.string.lists_empty))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(TGGeo.gridMinCell),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = TGSpace.l, end = TGSpace.l, top = TGSpace.l, bottom = TGSpace.xl),
                    horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
                    verticalArrangement = Arrangement.spacedBy(TGSpace.l),
                ) {
                    items(s.value.aircraftList.distinctBy { it.id }, key = { it.id }) { aircraft ->
                        val locked = aircraft.isVip && !user.isPremium
                        ListAircraftCard(aircraft, locked) {
                            when {
                                !locked -> onOpenAircraft(aircraft.id)
                                user.isLoggedIn -> onShowPaywall()
                                else -> onShowSignIn()
                            }
                        }
                    }
                }
            }
        }
    }
}

/** iOS CustomListAircraftCard: thumbnail (lock scrim when locked), two-line name and role. */
@Composable
fun ListAircraftCard(aircraft: Aircraft, isLocked: Boolean, onClick: () -> Unit) {
    val c = TG.colors
    Column(
        Modifier.fillMaxWidth().clip(TGGeo.shape12).background(c.textPrimary.copy(alpha = 0.04f))
            .clickable(role = Role.Button, onClick = onClick).padding(TGSpace.xs + TGSpace.xs / 2),
        verticalArrangement = Arrangement.spacedBy(TGSpace.xs + TGSpace.xs / 2),
    ) {
        Box(Modifier.fillMaxWidth().height(TGGeo.gridThumbHeight).clip(TGGeo.shape10)) {
            if (aircraft.thumbnail.isNullOrBlank()) {
                Image(painterResource(R.drawable.img_0_placeholder), stringResource(R.string.lists_photo, aircraft.name), Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                TGImage(aircraft.thumbnail, stringResource(R.string.lists_photo, aircraft.name), Modifier.fillMaxSize())
            }
            if (isLocked) LockScrim(Modifier.fillMaxSize())
        }
        Text(
            aircraft.name, style = TGText.sailecBold(12), color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().height(TGGeo.gridNameHeight),
        )
        aircraft.role?.takeIf { it.isNotEmpty() }?.let {
            Text(
                it, style = TGText.sailecRegular(10), color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().height(TGGeo.gridRoleHeight),
            )
        }
    }
}
