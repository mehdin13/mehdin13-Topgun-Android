package app.topgun.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TG
import app.topgun.android.features.auth.SignInScreen
import app.topgun.android.features.badges.BadgesScreen
import app.topgun.android.features.blog.BlogScreen
import app.topgun.android.features.countries.CountryDetailScreen
import app.topgun.android.features.countries.CountryListScreen
import app.topgun.android.features.details.AircraftDetailScreen
import app.topgun.android.features.favorites.FavoritesScreen
import app.topgun.android.features.home.HomeScreen
import app.topgun.android.features.intro.IntroScreen
import app.topgun.android.features.lists.CustomAircraftListScreen
import app.topgun.android.features.manufacturers.ManufacturerDetailScreen
import app.topgun.android.features.manufacturers.ManufacturerListScreen
import app.topgun.android.features.paywall.PaywallScreen
import app.topgun.android.features.settings.SettingsScreen
import app.topgun.android.features.splash.SplashScreen

/**
 * App flow (iOS `Top_GunApp`): Intro (first launch only) -> Splash (Lottie, ~3 s) -> Main tabs.
 * Sign-in and the paywall are screens pushed on top; they are not gates (browsing works without an account).
 */
@Composable
fun TopGunRoot() {
    val container = LocalAppContainer.current
    val nav = rememberNavController()
    val introDone by container.prefs.introDone.collectAsStateWithLifecycle()
    val signedIn by container.session.isSignedIn.collectAsStateWithLifecycle()

    // Profile and Pro flag are refreshed at launch (iOS fetchUserInfo); a 401 elsewhere flips signedIn and clears local state.
    LaunchedEffect(signedIn) {
        if (signedIn) container.user.refresh() else container.user.onSessionExpired()
    }

    val start = if (introDone) Routes.Gate else "intro"
    NavHost(navController = nav, startDestination = start) {
        composable("intro") {
            IntroScreen(onFinished = {
                container.prefs.setIntroDone(true)
                nav.navigate(Routes.Gate) { popUpTo("intro") { inclusive = true } }
            })
        }
        composable(Routes.Gate) {
            SplashScreen(onFinished = { nav.navigate(Routes.Main) { popUpTo(Routes.Gate) { inclusive = true } } })
        }
        composable(Routes.Main) {
            MainShell(
                onOpenAircraft = { nav.navigate(Routes.aircraft(it)) },
                onOpenCountries = { nav.navigate(Routes.Countries) },
                onOpenManufacturers = { nav.navigate(Routes.Manufacturers) },
                onOpenList = { nav.navigate(Routes.list(it)) },
                onOpenBlog = { nav.navigate(Routes.Blog) },
                onOpenBadges = { nav.navigate(Routes.Badges) },
                onShowSignIn = { nav.navigate(Routes.SignIn) },
                onShowPaywall = { nav.navigate(Routes.Paywall) },
            )
        }
        composable(Routes.SignIn) { SignInScreen(onDismiss = { nav.popBackStack() }) }
        composable(Routes.Paywall) { PaywallScreen(onDismiss = { nav.popBackStack() }) }
        composable(Routes.Countries) {
            CountryListScreen(onBack = { nav.popBackStack() }, onOpenCountry = { nav.navigate(Routes.country(it)) })
        }
        composable(Routes.Manufacturers) {
            ManufacturerListScreen(onBack = { nav.popBackStack() }, onOpenManufacturer = { nav.navigate(Routes.manufacturer(it)) })
        }
        composable(Routes.Blog) { BlogScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.Badges) { BadgesScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.AircraftPattern, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            AircraftDetailScreen(
                id = id,
                onBack = { nav.popBackStack() },
                onOpenAircraft = { nav.navigate(Routes.aircraft(it)) },
                onOpenCountry = { nav.navigate(Routes.country(it)) },
                onOpenManufacturer = { nav.navigate(Routes.manufacturer(it)) },
                onShowSignIn = { nav.navigate(Routes.SignIn) },
                onShowPaywall = { nav.navigate(Routes.Paywall) },
            )
        }
        composable(Routes.CountryPattern, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            CountryDetailScreen(
                id = id,
                onBack = { nav.popBackStack() },
                onOpenAircraft = { nav.navigate(Routes.aircraft(it)) },
                onOpenManufacturer = { nav.navigate(Routes.manufacturer(it)) },
            )
        }
        composable(Routes.ManufacturerPattern, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            ManufacturerDetailScreen(id = id, onBack = { nav.popBackStack() }, onOpenAircraft = { nav.navigate(Routes.aircraft(it)) })
        }
        composable(Routes.ListPattern, arguments = listOf(navArgument("name") { type = NavType.StringType })) { entry ->
            val name = java.net.URLDecoder.decode(entry.arguments?.getString("name").orEmpty(), "UTF-8")
            CustomAircraftListScreen(
                name = name,
                onBack = { nav.popBackStack() },
                onOpenAircraft = { nav.navigate(Routes.aircraft(it)) },
                onShowSignIn = { nav.navigate(Routes.SignIn) },
                onShowPaywall = { nav.navigate(Routes.Paywall) },
            )
        }
    }
}

/** The three tabs (Home, Favorites, Settings) with the floating rounded tab bar. */
@Composable
private fun MainShell(
    onOpenAircraft: (Int) -> Unit,
    onOpenCountries: () -> Unit,
    onOpenManufacturers: () -> Unit,
    onOpenList: (String) -> Unit,
    onOpenBlog: () -> Unit,
    onOpenBadges: () -> Unit,
    onShowSignIn: () -> Unit,
    onShowPaywall: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var tabBarVisible by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize().background(TG.colors.background), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(onOpenAircraft, onOpenCountries, onOpenManufacturers, onOpenList, onShowSignIn, onShowPaywall, onSetTabBarVisible = { tabBarVisible = it })
                1 -> FavoritesScreen(onOpenAircraft, onShowSignIn, onShowPaywall)
                else -> SettingsScreen(onOpenBlog, onOpenBadges, onShowSignIn, onShowPaywall)
            }
        }
        TopGunTabBar(selected = tab, onSelect = { tab = it; tabBarVisible = true }, visible = tabBarVisible)
    }
}
