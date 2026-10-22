package app.topgun.android.data

import app.topgun.android.platform.PlatformServices
import app.topgun.android.platform.ProStatus
import app.topgun.core.auth.SessionStore
import app.topgun.core.model.UserInfo
import app.topgun.core.network.ApiError
import app.topgun.core.network.TopGunApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Signed-in state the whole UI reads: login flag, Pro flag and the cached profile. */
data class UserState(
    val isLoggedIn: Boolean = false,
    val isPremium: Boolean = false,
    val email: String? = null,
    val firstName: String? = null,
    val info: UserInfo? = null,
)

/**
 * Sign-in, sign-out, profile and Pro status. Google sign-in goes through [PlatformServices]; the backend
 * (`users/social_network_register/`) then returns the API token, which the session store keeps.
 */
class UserRepository(
    private val api: TopGunApi,
    private val session: SessionStore,
    private val prefs: AppPreferences,
    private val platform: PlatformServices,
) {
    private val _state = MutableStateFlow(
        UserState(session.token() != null, prefs.isPremium, prefs.userEmail, prefs.userFirstName),
    )
    val state: StateFlow<UserState> = _state.asStateFlow()

    /** Returns true when signed in, false when the user cancelled. Throws [ApiError] / platform errors otherwise. */
    suspend fun signInWithGoogle(): Boolean {
        val account = platform.signInWithGoogle() ?: return false
        val reply = api.socialLogin("google", account.email, account.firstName, account.lastName)
        val token = reply.token ?: throw ApiError.Validation(reply.message)
        session.signIn(token)
        prefs.userEmail = account.email
        prefs.userFirstName = account.firstName
        prefs.isPremium = reply.isVip
        reply.userId?.let { prefs.userId = it }
        _state.value = UserState(true, reply.isVip, account.email, account.firstName)
        return true
    }

    /** Loads the profile and the Pro flag (done at launch, like iOS `fetchUserInfo`). Failures keep the cached state. */
    suspend fun refresh() {
        if (session.token() == null) { _state.value = UserState(); return }
        val info = runCatching { api.userInfo() }.getOrNull() ?: return
        prefs.isPremium = info.isVip
        prefs.userFirstName = info.firstName
        prefs.userEmail = info.email
        prefs.userId = info.userId
        _state.value = UserState(true, info.isVip, info.email, info.firstName, info)
    }

    suspend fun signOut() {
        runCatching { api.logout() }          // best effort: revoke the token on the server
        runCatching { platform.signOutGoogle() }
        clearLocal()
    }

    fun onSessionExpired() = clearLocal()

    private fun clearLocal() {
        session.signOut()
        prefs.isPremium = false
        prefs.userEmail = null
        prefs.userFirstName = null
        prefs.userId = 0
        _state.value = UserState()
    }

    /**
     * After a purchase or a restore: tells the backend how long the subscription lasts and marks the user as Pro.
     * Note the backend's `make_vip` call is unauthenticated (docs/BACKEND_GAPS.md); the RevenueCat webhook is the real source.
     */
    suspend fun applyProStatus(status: ProStatus) {
        if (status is ProStatus.Pro) {
            prefs.isPremium = true
            _state.value = _state.value.copy(isPremium = true)
            val id = prefs.userId
            if (id != 0) runCatching { api.makeVip(id, status.days) }
        }
    }
}
