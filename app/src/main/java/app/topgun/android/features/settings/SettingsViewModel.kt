package app.topgun.android.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.AppPreferences
import app.topgun.android.data.Appearance
import app.topgun.android.data.UserRepository
import app.topgun.android.platform.ModelCacheInfo
import app.topgun.android.platform.PlatformServices
import app.topgun.android.platform.ProStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/** iOS `SettingsView` state: cache info, restore purchases, notifications, theme and logout. */
class SettingsViewModel(
    private val user: UserRepository,
    private val prefs: AppPreferences,
    private val platform: PlatformServices,
) : ViewModel() {
    private val _cache = MutableStateFlow(ModelCacheInfo(0, 0L))
    val cache: StateFlow<ModelCacheInfo> = _cache.asStateFlow()

    /** One-shot message shown as a snackbar (iOS Toast). */
    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val _restoring = MutableStateFlow(false)
    val restoring: StateFlow<Boolean> = _restoring.asStateFlow()

    init { refreshCache() }

    fun refreshCache() { _cache.value = platform.modelCacheInfo() }

    fun clearCache() {
        try {
            platform.clearModelCache()
            _toast.value = "Cache cleared. All 3D models removed."
        } catch (e: Exception) {
            _toast.value = "Failed to clear cache."
        }
        refreshCache()
    }

    fun setNotifications(enabled: Boolean) {
        prefs.setNotificationsEnabled(enabled)
        platform.setNotificationsEnabled(enabled)
    }

    fun setAppearance(value: Appearance) = prefs.setAppearance(value)

    fun restorePurchases() {
        if (_restoring.value) return
        _restoring.value = true
        viewModelScope.launch {
            try {
                val status = platform.restorePurchases()
                user.applyProStatus(status)
                _toast.value = if (status is ProStatus.Pro) "Subscription restored." else "No active subscription found."
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _toast.value = "Restore failed. Please try again."
            } finally {
                _restoring.value = false
            }
        }
    }

    fun signOut() { viewModelScope.launch { user.signOut() } }

    fun dismissToast() { _toast.value = null }

    companion object {
        /** iOS `CacheManager.getFormattedCacheSize`. */
        fun formatBytes(bytes: Long): String = when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024 -> String.format(Locale.US, "%.2f KB", bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
