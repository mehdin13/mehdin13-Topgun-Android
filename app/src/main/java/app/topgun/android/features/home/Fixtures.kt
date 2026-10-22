package app.topgun.android.features.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import app.topgun.android.data.AppContainer
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TopGunTheme
import app.topgun.android.platform.GoogleAccount
import app.topgun.android.platform.KeyValueStore
import app.topgun.android.platform.ModelCacheInfo
import app.topgun.android.platform.PlatformServices
import app.topgun.android.platform.ProStatus
import app.topgun.core.auth.InMemoryTokenStore
import app.topgun.core.model.Aircraft
import app.topgun.core.model.CountryBrief
import app.topgun.core.model.ManufacturerBrief
import app.topgun.core.network.ApiError

/** Preview data only; nothing in the app's runtime paths uses this file. */
internal object HomeFixtures {
    val aircraft = listOf(
        Aircraft(1, "F-22 Raptor", manufacturers = listOf(ManufacturerBrief(1, name = "Lockheed Martin")), countries = listOf(CountryBrief(1, "us", "United States")), generation = "5th", produced = "1997-2011"),
        Aircraft(2, "Su-57 Felon", manufacturers = listOf(ManufacturerBrief(2, name = "Sukhoi")), countries = listOf(CountryBrief(2, "ru", "Russia")), generation = "5th", produced = "2020-", isFavorite = true),
        Aircraft(3, "B-2 Spirit", manufacturers = listOf(ManufacturerBrief(3, name = "Northrop Grumman")), countries = listOf(CountryBrief(1, "us", "United States")), generation = "5th", produced = "1989-2000", isVip = true),
        Aircraft(4, "Eurofighter Typhoon", manufacturers = listOf(ManufacturerBrief(4, name = "Eurofighter GmbH")), countries = listOf(CountryBrief(3, "gb", "United Kingdom")), generation = "4.5th", produced = "2003-"),
    )

    val loaded = HomeState(items = aircraft, isLoading = false, hasMore = true)
    val loading = HomeState(isLoading = true)
    val failed = HomeState(isLoading = false, error = ApiError.Network)
    val empty = HomeState(isLoading = false, source = HomeSource.Search("zzz"))

    val noopActions = AircraftRowActions(onOpen = {}, onLockedTap = {}, onToggleFavorite = {}, onShare = {})

    /** A container that never touches the network, so [app.topgun.android.designsystem.components.TGImage] can resolve URLs. */
    val container: AppContainer = AppContainer(
        apiBaseUrl = "https://api.example.com/api/v1/",
        tokenStore = InMemoryTokenStore(),
        keyValueStore = object : KeyValueStore {
            override fun getString(key: String): String? = null
            override fun putString(key: String, value: String?) {}
            override fun getBoolean(key: String, default: Boolean): Boolean = default
            override fun putBoolean(key: String, value: Boolean) {}
        },
        platform = object : PlatformServices {
            override suspend fun signInWithGoogle(): GoogleAccount? = null
            override suspend fun signOutGoogle() {}
            override fun openUrl(url: String) {}
            override fun share(text: String) {}
            override fun openRateApp() {}
            override fun sendEmail(to: String, subject: String) {}
            override fun vibrate() {}
            override suspend fun currentProStatus(): ProStatus = ProStatus.Free
            override suspend fun restorePurchases(): ProStatus = ProStatus.Free
            override fun modelCacheInfo(): ModelCacheInfo = ModelCacheInfo(0, 0L)
            override fun clearModelCache() {}
            override fun setNotificationsEnabled(enabled: Boolean) {}
        },
    )
}

@Composable
private fun HomePreview(state: HomeState, isPremium: Boolean = false, searchText: String = "") {
    CompositionLocalProvider(LocalAppContainer provides HomeFixtures.container) {
        TopGunTheme {
            HomeContent(
                state = state, searchText = searchText, isPremium = isPremium, isRefreshing = false,
                snackbar = remember { SnackbarHostState() }, actions = HomeFixtures.noopActions,
                onSearchTextChange = {}, onSearch = {}, onSelectCategory = {}, onOpenFeature = {}, onOpenFilter = {},
                onRefresh = {}, onRetry = {}, onShowAll = {}, onLoadMore = {}, onSetTabBarVisible = {},
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HomeLoadedPreview() = HomePreview(HomeFixtures.loaded.copy(source = HomeSource.Category("Fighter")))

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HomeProPreview() = HomePreview(HomeFixtures.loaded, isPremium = true, searchText = "raptor")

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeLoadingPreview() = HomePreview(HomeFixtures.loading)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeErrorPreview() = HomePreview(HomeFixtures.failed)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeNoResultsPreview() = HomePreview(HomeFixtures.empty, searchText = "zzz")

@Preview(showBackground = true, heightDp = 2400)
@Composable
private fun FilterSheetPreview() {
    CompositionLocalProvider(LocalAppContainer provides HomeFixtures.container) {
        TopGunTheme {
            FilterSheetContent(
                state = FilterState(category = "Fighter", generations = listOf("5th"), speed = SpeedCategory.Supersonic, priceMax = 150f),
                isPremium = false, onChange = {}, onReset = {}, onDone = {}, onUpgrade = {},
            )
        }
    }
}
