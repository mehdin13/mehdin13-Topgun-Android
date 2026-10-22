package app.topgun.android.features.intro

import androidx.lifecycle.ViewModel
import app.topgun.android.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One slide of the welcome story (iOS `WelcomeAppStoryViewModel.WelcomeModel`). */
data class WelcomeSlide(val image: Int, val title: String, val subtitle: String)

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
            WelcomeSlide(R.drawable.page_1, "Welcome to Top Gun", "Explore advanced fighter aircraft, helicopters and spy planes."),
            WelcomeSlide(R.drawable.page_2, "Every detail", "Specs, history, 3D models and performance for each aircraft."),
            WelcomeSlide(R.drawable.page_3, "Save and watch", "Keep your favorites and watch videos and galleries."),
            WelcomeSlide(R.drawable.page_4, "Ready for take-off?", "Sign in to comment, rate and unlock Pro."),
        )
    }
}
