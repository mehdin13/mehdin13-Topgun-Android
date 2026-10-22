package app.topgun.android.features.intro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGEntry
import app.topgun.android.designsystem.TGEntryColors
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.components.EntryPillButton
import kotlinx.coroutines.delay

/**
 * Auto-advancing story pager (iOS `View/WelcomeAppStory/WelcomeAppStory.swift`): slides that change every 3 s, a row
 * of progress bars on top and the action buttons at the bottom. It is not wired into the navigation graph because
 * the iOS app never shows it either (only the template's own preview does). [onContinue] replaces both iOS buttons
 * ("Log In" and "Sign Up" just dismissed the view); sign-in on Android is the Google sheet.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeStoryScreen(onContinue: () -> Unit) {
    val vm = containerViewModel { WelcomeStoryViewModel() }
    val selection by vm.selection.collectAsStateWithLifecycle()
    val pager = rememberPagerState(pageCount = { vm.slides.size })

    // Timer: advance every 3 s. Restarts after a manual swipe because `selection` changes.
    LaunchedEffect(selection) {
        delay(TGEntry.welcomeAutoAdvanceMs)
        vm.advance()
    }
    LaunchedEffect(selection) { pager.animateScrollToPage(selection) }
    LaunchedEffect(pager.currentPage) { vm.select(pager.currentPage) }

    WelcomeStoryContent(vm.slides, selection, pager, onContinue)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WelcomeStoryContent(
    slides: List<WelcomeSlide>,
    selection: Int,
    pager: androidx.compose.foundation.pager.PagerState,
    onContinue: () -> Unit,
) {
    Box(Modifier.fillMaxSize().systemBarsPadding()) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            val slide = slides[page]
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TGSpace.s, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(slide.title, style = TGEntry.welcomeTitleStyle, color = TG.colors.textPrimary, maxLines = 2, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
                Text(
                    slide.subtitle,
                    style = TGEntry.welcomeSubtitleStyle,
                    color = TG.colors.textSecondary,
                    maxLines = 3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(TGEntry.welcomeSubtitleWidth).padding(bottom = TGEntry.welcomeSubtitleBottom),
                )
                Image(painterResource(slide.image), contentDescription = slide.title, contentScale = ContentScale.Fit, modifier = Modifier.height(TGEntry.welcomeImageHeight))
            }
        }
        // Progress bars on top (iOS `buildPagerFrame`).
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(TGSpace.l)
                .clearAndSetSemantics { contentDescription = "Story ${selection + 1} of ${slides.size}" },
            horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
        ) {
            slides.indices.forEach { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(TGEntry.welcomeBarHeight)
                        .background(
                            if (index == selection) (if (TG.colors.isDark) TG.colors.accent else TGEntryColors.black) else TGBrand.formGray,
                            RoundedCornerShape(TGEntry.welcomeBarRadius),
                        ),
                )
            }
        }
        EntryPillButton(
            "Continue",
            onContinue,
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(TGSpace.l),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
private fun WelcomeStoryPreview() {
    val slides = WelcomeStoryViewModel.defaultSlides
    WelcomeStoryContent(slides, 0, rememberPagerState(pageCount = { slides.size }), {})
}
