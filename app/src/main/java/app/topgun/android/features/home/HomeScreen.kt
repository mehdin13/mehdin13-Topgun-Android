package app.topgun.android.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.R
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.asApiError
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGHomeSize
import app.topgun.android.designsystem.TGHomeSpace
import app.topgun.android.designsystem.TGSpace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Home tab (iOS `HomeView`): logo, search + filter, "Welcome on board!" cards, category chips and the aircraft
 * list with Load More, pull to refresh, long-press favorite/share and the Pro lock. Scrolling down hides the tab bar.
 */
@Composable
fun HomeScreen(
    onOpenAircraft: (Int) -> Unit,
    onOpenCountries: () -> Unit,
    onOpenManufacturers: () -> Unit,
    onOpenList: (String) -> Unit,
    onShowSignIn: () -> Unit,
    onShowPaywall: () -> Unit,
    onSetTabBarVisible: (Boolean) -> Unit,
) {
    val container = LocalAppContainer.current
    val vm = containerViewModel("home") { HomeViewModel(it.api) }
    val filterVm = containerViewModel("home-filter") { FilterViewModel() }
    val state by vm.state.collectAsStateWithLifecycle()
    val filterState by filterVm.state.collectAsStateWithLifecycle()
    val user by container.user.state.collectAsStateWithLifecycle()

    val addedMessage = stringResource(R.string.home_toast_added_favorite)
    val removedMessage = stringResource(R.string.home_toast_removed_favorite)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var searchText by rememberSaveable { mutableStateOf("") }
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }

    fun toast(message: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    fun toggleFavorite(aircraft: app.topgun.core.model.Aircraft) {
        if (!user.isLoggedIn) { onShowSignIn(); return }
        scope.launch {
            try {
                toast(if (vm.toggleFavorite(aircraft.id)) addedMessage else removedMessage)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                toast(e.asApiError().userMessage)
            }
        }
    }

    // A failed Load More keeps the list and tells the user.
    LaunchedEffect(state.paginationError) {
        state.paginationError?.let { toast(it.userMessage); vm.clearPaginationError() }
    }

    val actions = AircraftRowActions(
        onOpen = { onOpenAircraft(it.id) },
        onLockedTap = { if (user.isLoggedIn) onShowPaywall() else onShowSignIn() },
        onToggleFavorite = ::toggleFavorite,
        onShare = { a ->
            val image = container.media.resolve(a.thumbnail)
            container.platform.share(if (image != null) "${a.name}\n$image" else a.name)
        },
    )

    HomeContent(
        state = state,
        searchText = searchText,
        isPremium = user.isPremium,
        isRefreshing = refreshing,
        snackbar = snackbar,
        actions = actions,
        onSearchTextChange = { searchText = it },
        onSearch = { query -> searchText = query; vm.search(query) },
        onSelectCategory = { searchText = ""; vm.selectCategory(it) },
        onOpenFeature = {
            when (it) {
                FeatureTarget.Countries -> onOpenCountries()
                FeatureTarget.Manufacturers -> onOpenManufacturers()
                is FeatureTarget.CustomList -> onOpenList(it.name)
            }
        },
        onOpenFilter = { showFilter = true },
        onRefresh = {
            scope.launch {
                refreshing = true
                container.platform.vibrate()
                searchText = ""
                try { vm.refresh() } finally { refreshing = false }
            }
        },
        onRetry = vm::retry,
        onShowAll = vm::showAll,
        onLoadMore = vm::loadMore,
        onSetTabBarVisible = onSetTabBarVisible,
    )

    if (showFilter) {
        FilterSheet(
            state = filterState,
            isPremium = user.isPremium,
            onChange = { next -> filterVm.update { next } },
            onReset = filterVm::reset,
            onDone = {
                showFilter = false
                searchText = ""
                vm.applyFilter(filterState.toFilter(user.isPremium))
            },
            onDismiss = { showFilter = false },
            onUpgrade = {
                showFilter = false
                if (user.isLoggedIn) onShowPaywall() else onShowSignIn()
            },
        )
    }
}

/** Stateless Home layout, shared by the screen and the previews. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    searchText: String,
    isPremium: Boolean,
    isRefreshing: Boolean,
    snackbar: SnackbarHostState,
    actions: AircraftRowActions,
    onSearchTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenFeature: (FeatureTarget) -> Unit,
    onOpenFilter: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onShowAll: () -> Unit,
    onLoadMore: () -> Unit,
    onSetTabBarVisible: (Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    val setTabBarVisible by rememberUpdatedState(onSetTabBarVisible)
    val thresholdPx = with(LocalDensity.current) { TGHomeSize.tabBarScrollThreshold.toPx() }

    // iOS ScrollOffsetPreferenceKey: scrolling down by more than the threshold hides the tab bar, scrolling up shows it.
    val connection = remember(thresholdPx) {
        object : NestedScrollConnection {
            private var travelled = 0f
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy == 0f) return Offset.Zero
                if ((dy < 0f) != (travelled < 0f)) travelled = 0f
                travelled += dy
                if (travelled < -thresholdPx) setTabBarVisible(false) else if (travelled > thresholdPx) setTabBarVisible(true)
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop -> if (atTop) setTabBarVisible(true) }
    }
    DisposableEffect(Unit) { onDispose { setTabBarVisible(true) } }

    Box(Modifier.fillMaxSize().background(TG.colors.background)) {
        PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Box(Modifier.fillMaxSize().nestedScroll(connection)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = TGSpace.l),
                    verticalArrangement = Arrangement.spacedBy(TGHomeSpace.section),
                ) {
                    item(key = "header") { HomeHeader() }
                    item(key = "search") { SearchAndFilterRow(searchText, onSearchTextChange, onSearch, onOpenFilter) }
                    item(key = "welcome") { WelcomeSection(onOpenFeature) }
                    item(key = "categories") { CategoriesSection(state.selectedCategory, onSelectCategory) }
                    aircraftContent(state, isPremium, actions, onRetry, onShowAll, onLoadMore)
                    item(key = "bottom") { Spacer(Modifier.height(TGSpace.tabBarClearance)) }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = TGSpace.tabBarClearance))
    }
}
