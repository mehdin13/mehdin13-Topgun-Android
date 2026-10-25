// ANDROID-ONLY
package app.topgun.android

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import app.topgun.android.data.Appearance
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TopGunTheme
import app.topgun.android.navigation.TopGunRoot
import app.topgun.android.platform.ui.AndroidPlatformUi
import app.topgun.android.platform.ui.LocalPlatformUi

class MainActivity : ComponentActivity() {
    /** Applies the language chosen in Settings (or the device language) to every string resource and the layout direction. */
    override fun attachBaseContext(newBase: Context) {
        val prefs = (newBase.applicationContext as? TopGunApp)?.container?.prefs
        val chosen = prefs?.language?.value
        if (chosen == null) { super.attachBaseContext(newBase); return }
        val locale = Locale.forLanguageTag(chosen.code)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as TopGunApp).container
        val shownLanguage = container.prefs.language.value
        lifecycleScope.launch {
            container.prefs.language.drop(1).collect { if (it != shownLanguage) recreate() }
        }
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
