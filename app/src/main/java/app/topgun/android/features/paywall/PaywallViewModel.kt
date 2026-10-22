package app.topgun.android.features.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.UserRepository
import app.topgun.android.platform.ProStatus
import kotlinx.coroutines.launch

/** Tells the backend and the local state about a finished purchase (iOS `handlePurchaseCompletion`). */
class PaywallViewModel(private val user: UserRepository) : ViewModel() {
    private var handled = false

    /** [days] comes from the purchased product. [done] runs after the Pro flag is stored; failures of the backend call are swallowed by the repository. */
    fun onPurchased(days: Int, done: () -> Unit) {
        if (handled) return
        handled = true
        viewModelScope.launch {
            user.applyProStatus(ProStatus.Pro(days))
            done()
        }
    }
}
