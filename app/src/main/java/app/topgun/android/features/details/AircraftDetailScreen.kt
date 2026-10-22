package app.topgun.android.features.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Load
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.DetailCircleButton
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.core.network.ApiError
import kotlinx.coroutines.delay

/** Sub-pages of the detail screen. iOS pushes these with NavigationLink / fullScreenCover; here they swap in place and Back returns to the detail. */
internal enum class DetailPage { Main, Gallery, DeckViewer, Specs, Videos, Operators, Pdf, Model, Comments }

/**
 * iOS `DetailsView` (the live one; `DetailsView 2.swift` is not in the Xcode target) with its pushed pages:
 * gallery, specification, videos, operators map, PDF, 3D model and comments.
 */
@Composable
fun AircraftDetailScreen(
    id: Int,
    onBack: () -> Unit,
    onOpenAircraft: (Int) -> Unit,
    onOpenCountry: (Int) -> Unit,
    onOpenManufacturer: (Int) -> Unit,
    onShowSignIn: () -> Unit,
    onShowPaywall: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm = containerViewModel("details-$id") { DetailsViewModel(it.api, id) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val user by container.user.state.collectAsStateWithLifecycle()
    var pageIndex by rememberSaveable { mutableStateOf(0) }
    var pageArg by rememberSaveable { mutableStateOf(0) }
    val page = DetailPage.entries[pageIndex.coerceIn(0, DetailPage.entries.size - 1)]
    val open: (DetailPage, Int) -> Unit = { p, arg -> pageArg = arg; pageIndex = p.ordinal }

    BackHandler(enabled = page != DetailPage.Main) {
        // The full-screen deck viewer sits on top of the detail page; every other page also returns to it.
        pageIndex = DetailPage.Main.ordinal
    }

    val message = ui.toast ?: ui.error
    LaunchedEffect(message) {
        if (message != null) {
            delay(2000)
            vm.toastShown()
            vm.errorShown()
        }
    }

    Box(Modifier.fillMaxSize().background(TG.colors.background)) {
        when (val loaded = ui.detail) {
            Load.Loading -> Column(Modifier.fillMaxSize().statusBarsPadding()) {
                BackRow(onBack)
                LoadingState("Loading aircraft...", Modifier.weight(1f))
            }
            is Load.Failed -> Column(Modifier.fillMaxSize().statusBarsPadding()) {
                BackRow(onBack)
                val error = loaded.error
                if (error is ApiError.Forbidden && error.isVipRequired) {
                    VipLocked(signedIn = user.isLoggedIn, onUnlock = { if (user.isLoggedIn) onShowPaywall() else onShowSignIn() })
                } else {
                    ErrorState(error.userMessage, Modifier.weight(1f), onRetry = { vm.load() })
                }
            }
            is Load.Loaded -> {
                val actions = DetailActions(
                    onBack = onBack,
                    open = open,
                    onFavorite = { if (user.isLoggedIn) vm.toggleFavorite() else onShowSignIn() },
                    onLike = { if (user.isLoggedIn) vm.toggleLike() else onShowSignIn() },
                    onRate = vm::rate,
                    onOpenAircraft = onOpenAircraft,
                    onOpenCountry = onOpenCountry,
                    onOpenManufacturer = onOpenManufacturer,
                    onShowSignIn = onShowSignIn,
                )
                when (page) {
                    DetailPage.Main -> DetailMain(loaded.value, ui, user, actions)
                    else -> DetailSubPage(page, pageArg, loaded.value, ui, user, actions, onClose = { pageIndex = DetailPage.Main.ordinal })
                }
            }
        }

        if (message != null) {
            Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = TGSpace.xxl).padding(horizontal = TGSpace.l)) {
                Column(
                    Modifier.background(TGBrand.backdropBlack.copy(alpha = 0.9f), TGRadius.card).padding(horizontal = TGSpace.l, vertical = TGSpace.m),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(message, style = TGText.sailecBold(14), color = TGDetailColors.onPhoto, textAlign = TextAlign.Center)
                    if (ui.toast != null) Text("TopGun", style = TGText.sailecRegular(12), color = TGDetailColors.infoText)
                }
            }
        }
    }
}

@Composable
private fun BackRow(onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = TGSpace.s, vertical = TGSpace.s)) {
        DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
    }
}

/** VIP aircraft opened without Pro: the backend answers 403 "VIP". */
@Composable
private fun VipLocked(signedIn: Boolean, onUnlock: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(TGSpace.xl),
        verticalArrangement = Arrangement.spacedBy(TGSpace.m, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = TGBrand.gold, modifier = Modifier.size(TGSize.tapMin))
        Text("Pro aircraft", style = TGText.sailecBold(22), color = TG.colors.textPrimary)
        Text(
            "This aircraft is for Top Gun Pro members.",
            style = TGText.sailecMedium(16), color = TG.colors.textSecondary, textAlign = TextAlign.Center,
        )
        Button(onClick = onUnlock, modifier = Modifier.padding(top = TGSpace.s)) {
            Text(if (signedIn) "Unlock with Pro" else "Sign in to continue", style = TGText.sailecMedium(16))
        }
    }
}

/** Callbacks the detail page and its sub-pages use. */
internal class DetailActions(
    val onBack: () -> Unit,
    val open: (DetailPage, Int) -> Unit,
    val onFavorite: () -> Unit,
    val onLike: () -> Unit,
    val onRate: (Int) -> Unit,
    val onOpenAircraft: (Int) -> Unit,
    val onOpenCountry: (Int) -> Unit,
    val onOpenManufacturer: (Int) -> Unit,
    val onShowSignIn: () -> Unit,
)

