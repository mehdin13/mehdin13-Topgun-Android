package app.topgun.android.data

import app.topgun.android.platform.KeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Appearance(val label: String) { System("System"), Light("Light"), Dark("Dark") }

/** Small user-visible settings and cached account flags, mirroring the iOS `@AppStorage` keys. Not secrets: the token is in the token store. */
class AppPreferences(private val store: KeyValueStore) {
    private val _appearance = MutableStateFlow(Appearance.entries.firstOrNull { it.name == store.getString(APPEARANCE) } ?: Appearance.System)
    val appearance: StateFlow<Appearance> = _appearance.asStateFlow()
    fun setAppearance(value: Appearance) { store.putString(APPEARANCE, value.name); _appearance.value = value }

    /** Chosen UI language, or null to follow the device language. */
    private val _language = MutableStateFlow(AppLanguage.fromCode(store.getString(LANGUAGE)))
    val language: StateFlow<AppLanguage?> = _language.asStateFlow()
    fun setLanguage(value: AppLanguage?) { store.putString(LANGUAGE, value?.code); _language.value = value }

    /** True once the intro/onboarding has been completed (iOS `isOnboarding`). */
    private val _introDone = MutableStateFlow(store.getBoolean(INTRO_DONE, false))
    val introDone: StateFlow<Boolean> = _introDone.asStateFlow()
    fun setIntroDone(value: Boolean) { store.putBoolean(INTRO_DONE, value); _introDone.value = value }

    private val _notifications = MutableStateFlow(store.getBoolean(NOTIFICATIONS, true))
    val notificationsEnabled: StateFlow<Boolean> = _notifications.asStateFlow()
    fun setNotificationsEnabled(value: Boolean) { store.putBoolean(NOTIFICATIONS, value); _notifications.value = value }

    var userEmail: String?
        get() = store.getString(EMAIL)
        set(value) = store.putString(EMAIL, value)
    var userFirstName: String?
        get() = store.getString(FIRST_NAME)
        set(value) = store.putString(FIRST_NAME, value)
    var isPremium: Boolean
        get() = store.getBoolean(PREMIUM, false)
        set(value) = store.putBoolean(PREMIUM, value)
    var userId: Int
        get() = store.getString(USER_ID)?.toIntOrNull() ?: 0
        set(value) = store.putString(USER_ID, if (value == 0) null else value.toString())

    private companion object {
        const val APPEARANCE = "appearance"
        const val LANGUAGE = "appLanguage"
        const val INTRO_DONE = "isOnboarding"
        const val NOTIFICATIONS = "notifications"
        const val EMAIL = "userEmail"
        const val FIRST_NAME = "userFirstName"
        const val PREMIUM = "isPremiumUser"
        const val USER_ID = "userId"
    }
}
