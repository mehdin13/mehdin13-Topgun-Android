package app.topgun.android.features.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGEntry
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.components.EntryPageIndicator
import app.topgun.android.designsystem.components.EntryPillButton
import androidx.compose.ui.tooling.preview.Preview

/**
 * First-launch walkthrough (iOS `View/Intro/Intro.swift` + `IntroView`). Three pages with dots and "Next", then
 * "Let's Get Started" with "Continue" which calls [onFinished] (iOS sets `isOnboarding` and shows the splash).
 * The email/password fields of the iOS file are commented out there and are not ported.
 */
@Composable
fun IntroScreen(onFinished: () -> Unit) {
    val vm = containerViewModel { IntroViewModel() }
    val state by vm.state.collectAsStateWithLifecycle()
    IntroContent(state = state, onNext = vm::next, onPrevious = vm::previous, onContinue = onFinished)
}

/** Animates to 1 when [on] turns true and snaps back to 0 when it turns false (iOS flips the flags without animation). */
@Composable
private fun rememberEntrySpring(on: Boolean): State<Float> {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(on) {
        if (on) anim.animateTo(1f, spring(TGEntry.springDamping, TGEntry.springStiffness)) else anim.snapTo(0f)
    }
    return anim.asState()
}

@Composable
internal fun IntroContent(state: IntroState, onNext: () -> Unit, onPrevious: () -> Unit, onContinue: () -> Unit) {
    val show by rememberEntrySpring(state.shown)
    val hide by rememberEntrySpring(state.hiding)
    val page = state.page
    val travel = with(LocalDensity.current) { TGEntry.backButtonTravel.toPx() }

    BoxWithConstraints(Modifier.fillMaxSize().systemBarsPadding().padding(TGEntry.pagePadding)) {
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
        Column(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = hide * heightPx / 2; alpha = 1f - hide },
        ) {
            // Illustration: comes from above.
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer { translationY = -(1f - show) * heightPx / 2; alpha = show },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(page.image),
                    contentDescription = page.title.replace("\n", " "),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(TGEntry.pagePadding),
                )
            }
            // Title, text and actions: come from below.
            Column(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = (1f - show) * heightPx / 2; alpha = show }
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TGSpace.s),
            ) {
                Text(
                    page.title,
                    style = TGEntry.titleStyle,
                    color = TG.colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    page.subtitle,
                    style = TGEntry.subtitleStyle,
                    color = TG.colors.textPrimary,
                    modifier = Modifier.padding(top = TGEntry.pagePadding),
                )
                if (!page.displaysAction) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                        EntryPageIndicator(state.indicatorCount, state.index)
                        EntryPillButton("Next", onNext, Modifier.fillMaxWidth(TGEntry.nextButtonWidthFraction))
                    }
                } else {
                    EntryPillButton("Continue", onContinue, Modifier.fillMaxWidth().padding(top = TGSpace.l))
                }
            }
        }
        // Back button: comes from above, leaves upward.
        if (state.canGoBack) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(TGSize.tapMin)
                    .graphicsLayer { translationY = -(1f - show) * travel - hide * travel }
                    .clickable(onClickLabel = "Previous page", onClick = onPrevious),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous page", tint = TG.colors.textPrimary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IntroPreview() {
    IntroContent(IntroState(index = 1, shown = true), {}, {}, {})
}
