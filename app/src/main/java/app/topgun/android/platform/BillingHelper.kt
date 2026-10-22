// ANDROID-ONLY
package app.topgun.android.platform

import android.content.Context
import app.topgun.android.BuildConfig
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitRestore

/**
 * RevenueCat (`Purchases`) wrapper. Configured lazily from `BuildConfig.REVENUECAT_API_KEY` (a *Google Play* public
 * SDK key, `goog_...`, not the iOS `appl_...` key); an empty key means "no purchases", never a crash.
 * Mirrors iOS `HomeView.handlePurchaseCompletion` / `calculateSubscriptionDays` / `restorePurchases`.
 */
class BillingHelper(private val context: Context) {

    /** Current entitlement. Free when billing is not configured or the request fails (offline). */
    suspend fun currentStatus(): ProStatus {
        if (!configureIfNeeded(context)) return ProStatus.Free
        return try { proStatus(Purchases.sharedInstance.awaitCustomerInfo()) } catch (e: Exception) { ProStatus.Free }
    }

    /** Restores purchases made with the same Google account (iOS `Purchases.shared.restorePurchases`). */
    suspend fun restore(): ProStatus {
        if (!configureIfNeeded(context)) return ProStatus.Free
        return try { proStatus(Purchases.sharedInstance.awaitRestore()) } catch (e: Exception) { ProStatus.Free }
    }

    companion object {
        /** Entitlement id configured in the RevenueCat dashboard (iOS `entitlements.all["Pro"]`). */
        const val ENTITLEMENT_ID = "Pro"

        /** Configures RevenueCat once. Returns false (and does nothing) when the API key is not set. */
        @Synchronized
        fun configureIfNeeded(context: Context): Boolean {
            if (Purchases.isConfigured) return true
            val key = BuildConfig.REVENUECAT_API_KEY
            if (key.isBlank()) return false
            Purchases.configure(PurchasesConfiguration.Builder(context.applicationContext, key).build())
            return true
        }

        /** Pro with the purchased period when the "Pro" entitlement is active, otherwise Free. */
        fun proStatus(info: CustomerInfo): ProStatus {
            val entitlement = info.entitlements.all[ENTITLEMENT_ID]
            if (entitlement?.isActive != true) return ProStatus.Free
            // iOS reads `activeSubscriptions.first`. The entitlement's own product also covers the lifetime
            // purchase, which RevenueCat does not list among the active *subscriptions*.
            val candidates = listOf(entitlement.productIdentifier) + info.activeSubscriptions
            val days = candidates.firstNotNullOfOrNull { daysFor(it) } ?: 0
            return ProStatus.Pro(days)
        }

        /**
         * iOS product ids: Topgun_499_6Month_2w0 = 180, Topgun_999_12Month_2w0 = 360, Topgun_2999_Lifetime = 9999.
         * Google Play ids must be lowercase and subscriptions come as "productId:basePlanId", so the id is
         * compared lowercased, without the base plan. Unknown products return null (iOS returns 0).
         */
        fun daysFor(productId: String): Int? {
            val id = productId.substringBefore(':').lowercase()
            return when {
                id == "topgun_499_6month_2w0" -> 180
                id == "topgun_999_12month_2w0" -> 360
                id == "topgun_2999_lifetime" -> 9999
                // Looser fallbacks for ids created on the Play side with a different suffix.
                "lifetime" in id -> 9999
                "12month" in id || "annual" in id || "yearly" in id -> 360
                "6month" in id || "sixmonth" in id -> 180
                else -> null
            }
        }
    }
}
