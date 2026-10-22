// ANDROID-ONLY
package app.topgun.android.platform

import android.content.Context

/** TODO(port, paywall agent): RevenueCat Purchases (entitlement "Pro"). Product ids and day counts: see iOS HomeView.calculateSubscriptionDays. */
class BillingHelper(private val context: Context) {
    suspend fun currentStatus(): ProStatus = ProStatus.Free
    suspend fun restore(): ProStatus = ProStatus.Free
}
