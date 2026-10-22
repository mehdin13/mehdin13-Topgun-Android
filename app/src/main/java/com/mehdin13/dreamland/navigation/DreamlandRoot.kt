package com.mehdin13.dreamland.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mehdin13.dreamland.data.rememberContainer
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.components.DLBottomBar
import com.mehdin13.dreamland.designsystem.components.DLIconName
import com.mehdin13.dreamland.designsystem.components.DLTab
import com.mehdin13.dreamland.features.feed.FeedScreen
import com.mehdin13.dreamland.features.home.HomeScreen
import com.mehdin13.dreamland.features.library.LibraryScreen
import com.mehdin13.dreamland.features.profile.ProfileScreen
import com.mehdin13.dreamland.features.signin.SignInScreen
import com.mehdin13.dreamland.features.story.StoryScreen
import kotlinx.coroutines.launch

private object Routes {
    const val Main = "main"
    const val Story = "story/{id}?owner={owner}"
    fun story(id: Int, owner: Boolean) = "story/$id?owner=$owner"
}

/** Signed out: sign-in. Signed in: the tab shell. A confirmed 401 flips the session and returns here. */
@Composable
fun DreamlandRoot() {
    val session = rememberContainer().session
    val signedIn by session.isSignedIn.collectAsStateWithLifecycle()
    if (signedIn) SignedInNav() else SignInScreen()
}

private val Tabs = listOf(
    DLTab(DLIconName.House, "Home"),
    DLTab(DLIconName.Compass, "Discover"),
    DLTab(DLIconName.Library, "Library"),
    DLTab(DLIconName.Person, "Profile"),
)

@Composable
private fun SignedInNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.Main) {
        composable(Routes.Main) {
            MainShell(onOpenStory = { id, owner -> nav.navigate(Routes.story(id, owner)) })
        }
        composable(
            Routes.Story,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("owner") { type = NavType.BoolType; defaultValue = false },
            ),
        ) { entry ->
            StoryScreen(
                id = entry.arguments?.getInt("id") ?: return@composable,
                isOwner = entry.arguments?.getBoolean("owner") ?: false,
                onBack = { nav.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainShell(onOpenStory: (Int, Boolean) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Create flows (idea, voice, painting) and the Making screen are next; see docs/PROGRESS.md.
    val comingSoon: () -> Unit = { scope.launch { snackbar.showSnackbar("Making stories is coming to Android next.") } }

    Scaffold(
        containerColor = DL.colors.backgroundPrimary,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { DLBottomBar(Tabs, selected = tab, onSelect = { tab = it }, onCreate = comingSoon) },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(onOpenStory, onCreate = comingSoon, onCreateFromPainting = comingSoon, onSeeAllDiscover = { tab = 1 }, contentPadding = padding)
                1 -> FeedScreen(onOpenStory, padding)
                2 -> LibraryScreen(onOpenStory, padding)
                else -> ProfileScreen(padding)
            }
        }
    }
}
