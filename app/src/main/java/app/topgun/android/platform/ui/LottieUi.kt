// ANDROID-ONLY
package app.topgun.android.platform.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/** Lottie from `res/raw/<rawName>.json` (e.g. "splashlogo"). [onFinished] fires once when a non-looping animation ends. */
internal object LottieUi {
    @Composable
    fun Play(rawName: String, modifier: Modifier, loop: Boolean, onFinished: () -> Unit) {
        val context = LocalContext.current
        val resId = remember(rawName) { context.resources.getIdentifier(rawName, "raw", context.packageName) }
        val finished by rememberUpdatedState(onFinished)

        if (resId == 0) {
            // Unknown animation: do not leave the caller waiting.
            LaunchedEffect(rawName) { finished() }
            return
        }

        val result = rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
        val composition = result.value
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = if (loop) LottieConstants.IterateForever else 1,
        )
        LottieAnimation(composition = composition, progress = { progress }, modifier = modifier)

        LaunchedEffect(composition, progress, result.isFailure) {
            if (!loop && (result.isFailure || (composition != null && progress >= 1f))) finished()
        }
    }
}
