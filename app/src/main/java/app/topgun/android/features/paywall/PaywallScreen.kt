package app.topgun.android.features.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.platform.ui.LocalPlatformUi

/**
 * Subscription paywall (iOS `PaywallView()` from RevenueCatUI, shown as a sheet in `HomeView`). The RevenueCat
 * paywall itself comes from [LocalPlatformUi]; it reports the purchased period in days (180 / 360 / 9999, see
 * `BillingHelper`), which is stored through [PaywallViewModel] before the screen closes.
 */
@Composable
fun PaywallScreen(onDismiss: () -> Unit) {
    val vm = containerViewModel { PaywallViewModel(it.user) }
    Box(Modifier.fillMaxSize().background(TG.colors.background)) {
        LocalPlatformUi.current.Paywall(
            modifier = Modifier.fillMaxSize(),
            onDismiss = onDismiss,
            onPurchased = { days -> vm.onPurchased(days, done = onDismiss) },
        )
    }
}
