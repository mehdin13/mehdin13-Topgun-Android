package androidx.navigation

import android.os.Bundle

enum class NavType { IntType, StringType, BoolType, LongType }

class NamedNavArgument(val name: String)

class NavArgumentBuilder {
    var type: NavType = NavType.StringType
    var defaultValue: Any? = null
    var nullable: Boolean = false
}

fun navArgument(name: String, builder: NavArgumentBuilder.() -> Unit): NamedNavArgument = NamedNavArgument(name).also { NavArgumentBuilder().builder() }

class NavBackStackEntry(val arguments: Bundle?)

class PopUpToBuilder { var inclusive: Boolean = false; var saveState: Boolean = false }

class NavOptionsBuilder {
    var launchSingleTop: Boolean = false
    var restoreState: Boolean = false
    fun popUpTo(route: String, builder: PopUpToBuilder.() -> Unit = {}) { PopUpToBuilder().builder() }
}

class NavHostController {
    fun navigate(route: String, builder: NavOptionsBuilder.() -> Unit = {}) { NavOptionsBuilder().builder() }
    fun popBackStack(): Boolean = true
}
typealias NavController = NavHostController
