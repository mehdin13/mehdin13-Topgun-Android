// ANDROID-ONLY
package app.topgun.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Appearance
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TopGunTheme
import app.topgun.android.navigation.TopGunRoot
import app.topgun.android.platform.ui.AndroidPlatformUi
import app.topgun.android.platform.ui.LocalPlatformUi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as TopGunApp).container
        setContent {
            val appearance by container.prefs.appearance.collectAsStateWithLifecycle()
            val dark = when (appearance) {
                Appearance.System -> isSystemInDarkTheme()
                Appearance.Light -> false
                Appearance.Dark -> true
            }
            CompositionLocalProvider(LocalAppContainer provides container, LocalPlatformUi provides AndroidPlatformUi) {
                TopGunTheme(darkTheme = dark) { TopGunRoot() }
            }
        }
    }
}
