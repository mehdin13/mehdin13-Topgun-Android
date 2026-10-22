package app.topgun.android.features.favorites

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Load
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AircraftListCard
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.FavoriteHeartButton
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.android.designsystem.components.TGImage
import app.topgun.android.designsystem.largeTitle
import app.topgun.core.model.Aircraft

/**
 * iOS `FavoriteView`: title, grid/list toggle, favorites with a heart that removes them.
 * VIP aircraft are locked unless the user is Pro (tap opens the paywall, or sign-in when signed out).
 */
@Composable
fun FavoritesScreen(onOpenAircraft: (Int) -> Unit, onShowSignIn: () -> Unit, onShowPaywall: () -> Unit) {
    val container = LocalAppContainer.current
    val vm = containerViewModel { FavoritesViewModel(it.api) }
    val user by container.user.state.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    var isGrid by rememberSaveable { mutableStateOf(true) }

    // iOS `.task { fetchFavorites() }`: reload each time the tab is shown or the account changes.
    LaunchedEffect(user.isLoggedIn) { if (user.isLoggedIn) vm.load() }

    val onTap: (Aircraft) -> Unit = { aircraft ->
        val locked = aircraft.isVip && !user.isPremium
        when {
            !locked -> onOpenAircraft(aircraft.id)
            user.isLoggedIn -> onShowPaywall()
            else -> onShowSignIn()
        }
    }

    Column(Modifier.fillMaxSize().background(TG.colors.background).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = TGSpace.l, vertical = TGSpace.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Favorite", style = TGText.largeTitle, color = TG.colors.textPrimary, modifier = Modifier.weight(1f))
            if (user.isLoggedIn && state is Load.Loaded) {
                Box(
                    Modifier.size(TGSize.tapMin).clip(CircleShape).clickable(role = Role.Button) { isGrid = !isGrid },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (isGrid) Icons.Filled.GridView else Icons.Filled.FormatListBulleted,
                        contentDescription = if (isGrid) "Switch to list view" else "Switch to grid view",
                        tint = TG.colors.accent,
                    )
                }
            }
        }

        if (!user.isLoggedIn) {
            SignedOutState(onShowSignIn)
            return@Column
        }

        message?.let {
            Text(
                it, style = TGText.sailecMedium(12), color = TGBrand.red,
                modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { vm.dismissMessage() }.padding(horizontal = TGSpace.l, vertical = TGSpace.s),
            )
        }

        when (val s = state) {
            Load.Loading -> LoadingState("Loading favorites...", Modifier.weight(1f))
            is Load.Failed -> ErrorState(s.error.userMessage, Modifier.weight(1f), onRetry = vm::load)
            is Load.Loaded -> {
                val items = s.value
                when {
                    items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // iOS ContentUnavailableView: magnifier + "No results found".
                        EmptyState("No results found", icon = Icons.Filled.FavoriteBorder)
                    }
                    isGrid -> FavoritesGrid(items, user.isPremium, onTap, vm::remove, Modifier.weight(1f))
                    else -> FavoritesList(items, user.isPremium, onTap, vm::remove, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SignedOutState(onShowSignIn: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(TGSpace.xl),
        verticalArrangement = Arrangement.spacedBy(TGSpace.m, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = TG.colors.textSecondary, modifier = Modifier.size(TGAccountSize.emptyIcon))
        Text("Sign in to see your favorite aircraft", style = TGText.sailecMedium(16), color = TG.colors.textPrimary, textAlign = TextAlign.Center)
        Button(onClick = onShowSignIn, shape = TGRadius.button, modifier = Modifier.heightIn(min = TGSize.tapMin)) {
            Text("Sign In", style = TGText.sailecBold(16))
        }
    }
}

@Composable
private fun FavoritesGrid(items: List<Aircraft>, isPremium: Boolean, onTap: (Aircraft) -> Unit, onRemove: (Aircraft) -> Unit, modifier: Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(TGAccountSize.favoriteGridMinCell),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = TGSpace.l, end = TGSpace.l, bottom = TGSpace.tabBarClearance),
        horizontalArrangement = Arrangement.spacedBy(TGAccountSize.gridSpacing),
        verticalArrangement = Arrangement.spacedBy(TGAccountSize.gridSpacing),
    ) {
        items(items, key = { it.id }) { aircraft ->
            FavoriteCard(aircraft, locked = aircraft.isVip && !isPremium, onClick = { onTap(aircraft) }, onRemove = { onRemove(aircraft) })
        }
    }
}

/** iOS `AircraftCard`: 180 high rounded image, name (max two lines, 40 high), heart at the top right. */
@Composable
private fun FavoriteCard(aircraft: Aircraft, locked: Boolean, onClick: () -> Unit, onRemove: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(TGSpace.s),
        ) {
            Box(Modifier.fillMaxWidth().height(TGAccountSize.favoriteCardImageHeight).clip(TGRadius.image)) {
                TGImage(aircraft.thumbnail, contentDescription = "${aircraft.name} photo", modifier = Modifier.fillMaxSize())
                if (locked) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Lock, contentDescription = "Pro only", tint = Color.White)
                    }
                }
            }
            Text(
                aircraft.name, style = TGText.sailecBold(15), color = TG.colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().height(TGAccountSize.favoriteCardNameHeight),
            )
        }
        FavoriteHeartButton(aircraft.name, onRemove, Modifier.align(Alignment.TopEnd).padding(TGSpace.xs))
    }
}

@Composable
private fun FavoritesList(items: List<Aircraft>, isPremium: Boolean, onTap: (Aircraft) -> Unit, onRemove: (Aircraft) -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = TGSpace.l, end = TGSpace.l, bottom = TGSpace.tabBarClearance),
        verticalArrangement = Arrangement.spacedBy(TGSpace.m),
    ) {
        items(items, key = { it.id }) { aircraft ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.xs)) {
                AircraftListCard(aircraft, isLocked = aircraft.isVip && !isPremium, onClick = { onTap(aircraft) }, modifier = Modifier.weight(1f))
                FavoriteHeartButton(aircraft.name, { onRemove(aircraft) })
            }
        }
    }
}
