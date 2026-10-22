package app.topgun.android.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.topgun.android.designsystem.TGEntry
import app.topgun.android.designsystem.TGEntryColors
import app.topgun.android.platform.ui.LocalPlatformUi
import kotlinx.coroutines.delay

/**
 * Splash (iOS `View/MainViews/SplashView.swift`): black screen with the `Splashlogo` Lottie (150 x 150, plays once),
 * then the main tabs after ~3 s. [onFinished] runs once: when the animation ends or after 3 s, whichever comes first
 * (iOS always waits the full 3 s; the animation is about 2.6 s long, and the timer also covers a Lottie that fails to load).
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    val done = remember { booleanArrayOf(false) }
    val finishOnce = { if (!done[0]) { done[0] = true; finish() } }

    LaunchedEffect(Unit) {
        delay(TGEntry.splashDurationMs)
        finishOnce()
    }

    Box(
        Modifier.fillMaxSize().background(TGEntryColors.black).semantics { contentDescription = "Top Gun" },
        contentAlignment = Alignment.Center,
    ) {
        LocalPlatformUi.current.Lottie(
            rawName = "splashlogo",
            modifier = Modifier.size(TGEntry.splashLogo),
            loop = false,
            onFinished = finishOnce,
        )
    }
}
