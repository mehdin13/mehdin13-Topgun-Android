// ANDROID-ONLY
package app.topgun.android.platform.ui

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.platform.BillingHelper
import app.topgun.android.platform.ProStatus
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

/**
 * RevenueCatUI `Paywall` (iOS `PaywallView().onPurchaseCompleted`). The paywall design comes from the RevenueCat
 * dashboard offering. A purchase or a restore of the "Pro" entitlement reports the covered days to [onPurchased].
 */
internal object PaywallUi {
    @Composable
    fun Show(modifier: Modifier, onDismiss: () -> Unit, onPurchased: (days: Int) -> Unit) {
        val context = LocalContext.current
        val configured = remember { BillingHelper.configureIfNeeded(context) }
        if (!configured) {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.paywall_unavailable),
                    style = TGText.sailecMedium(16),
                    color = TG.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(TGSpace.xl),
                )
            }
            return
        }

        val currentOnPurchased = rememberUpdatedState(onPurchased)
        val currentOnDismiss = rememberUpdatedState(onDismiss)
        val options = remember {
            PaywallOptions.Builder(dismissRequest = { currentOnDismiss.value() })
                .setShouldDisplayDismissButton(true)
                .setListener(object : PaywallListener {
                    override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) =
                        report(customerInfo)

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) = report(customerInfo)

                    private fun report(info: CustomerInfo) {
                        val status = BillingHelper.proStatus(info)
                        if (status is ProStatus.Pro) currentOnPurchased.value(status.days)
                    }
                })
                .build()
        }
        Box(modifier) { Paywall(options) }
    }
}
