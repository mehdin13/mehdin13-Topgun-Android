package androidx.navigation.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

class NavGraphBuilder {
    internal val routes = mutableListOf<String>()
}

fun NavGraphBuilder.composable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit,
) { routes += route }

@Composable
fun rememberNavController(): NavHostController = NavHostController()

@Composable
fun NavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    builder: NavGraphBuilder.() -> Unit,
) { NavGraphBuilder().builder() }
