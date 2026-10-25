package app.topgun.android.features.intro

import androidx.lifecycle.ViewModel
import app.topgun.android.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One slide of the welcome story (iOS `WelcomeAppStoryViewModel.WelcomeModel`). */
data class WelcomeSlide(val image: Int, val title: Int, val subtitle: Int)

/**
 * iOS `WelcomeAppStoryViewModel`: the selected slide plus the auto-advance step (the 3 s timer lives in the screen).
 * The iOS file is a leftover of a template ("Skuy", Indonesian copy, images `onboard1..4`) that the iOS app never shows:
 * the slides here reuse the Top Gun intro art and English copy instead.
 */
class WelcomeStoryViewModel(val slides: List<WelcomeSlide> = defaultSlides) : ViewModel() {
    private val _selection = MutableStateFlow(0)
    val selection: StateFlow<Int> = _selection.asStateFlow()

    fun select(index: Int) { _selection.value = index.coerceIn(0, slides.lastIndex) }

    /** iOS: `selection < 3 ? selection + 1 : 0`. */
    fun advance() { _selection.value = if (_selection.value < slides.lastIndex) _selection.value + 1 else 0 }

    companion object {
        val defaultSlides = listOf(
            WelcomeSlide(R.drawable.page_1, R.string.welcome_slide1_title, R.string.welcome_slide1_subtitle),
            WelcomeSlide(R.drawable.page_2, R.string.welcome_slide2_title, R.string.welcome_slide2_subtitle),
            WelcomeSlide(R.drawable.page_3, R.string.welcome_slide3_title, R.string.welcome_slide3_subtitle),
            WelcomeSlide(R.drawable.page_4, R.string.welcome_slide4_title, R.string.welcome_slide4_subtitle),
        )
    }
}
