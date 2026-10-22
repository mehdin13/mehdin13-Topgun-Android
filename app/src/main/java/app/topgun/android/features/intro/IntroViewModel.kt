package app.topgun.android.features.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.designsystem.TGEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Page state of the walkthrough (iOS `IntroView`: `intro`, `showView`, `hideWholeView`).
 * [shown] drives the slide-in of the page, [hiding] the slide-out before the next page replaces it.
 */
data class IntroState(val index: Int = 0, val shown: Boolean = false, val hiding: Boolean = false) {
    val page: IntroPage get() = introPages[index]
    /** Dots only count the pages that are not the final "get started" page. */
    val indicatorCount: Int get() = introPages.count { !it.displaysAction }
    val canGoBack: Boolean get() = index != 0
}

class IntroViewModel(private val pages: List<IntroPage> = introPages) : ViewModel() {
    private val _state = MutableStateFlow(IntroState())
    val state: StateFlow<IntroState> = _state.asStateFlow()
    private var change: Job? = null

    init {
        viewModelScope.launch {
            delay(TGEntry.introEnterDelayMs)
            _state.update { it.copy(shown = true) }
        }
    }

    fun next() = changeIntro(previous = false)
    fun previous() = changeIntro(previous = true)

    /** iOS `changeIntro`: slide the page out, swap it after 0.5 s, then slide the new one in. Taps during the swap are ignored. */
    private fun changeIntro(previous: Boolean) {
        if (change?.isActive == true) return
        change = viewModelScope.launch {
            _state.update { it.copy(hiding = true) }
            delay(TGEntry.introSwapDelayMs)
            _state.update { s ->
                val i = s.index
                val target = if (previous) (if (i != 0) i - 1 else 0) else (if (i != pages.lastIndex) i + 1 else pages.lastIndex)
                s.copy(index = target, hiding = false, shown = false)
            }
            delay(FRAME_MS) // let the hidden state render once so the page animates in from off-screen
            _state.update { it.copy(shown = true) }
        }
    }

    private companion object { const val FRAME_MS = 16L }
}
