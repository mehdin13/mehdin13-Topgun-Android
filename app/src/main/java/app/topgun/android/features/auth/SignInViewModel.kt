package app.topgun.android.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.UserRepository
import app.topgun.android.platform.GoogleSignInException
import app.topgun.core.network.ApiError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInState(
    val isLoading: Boolean = false,
    /** Friendly text, never raw server or Credential Manager details. */
    val error: String? = null,
    /** True once signed in; the screen then closes itself. */
    val signedIn: Boolean = false,
)

class SignInViewModel(private val user: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    /** Google account picker, then the backend login. A cancelled picker changes nothing. */
    fun signInWithGoogle() {
        if (_state.value.isLoading) return
        _state.update { SignInState(isLoading = true) }
        viewModelScope.launch {
            try {
                val signedIn = user.signInWithGoogle()
                _state.update { SignInState(signedIn = signedIn) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: GoogleSignInException) {
                _state.update { SignInState(error = googleMessage(e)) }
            } catch (e: ApiError) {
                _state.update { SignInState(error = serverMessage(e)) }
            } catch (e: Exception) {
                _state.update { SignInState(error = GENERIC_ERROR) }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    companion object {
        /** The Google step (account sheet) failed: nothing reached the Top Gun server. */
        fun googleMessage(e: GoogleSignInException): String = when (e.kind) {
            GoogleSignInException.Kind.NotConfigured ->
                "Google sign-in is not set up in this build (missing Google web client ID)."
            GoogleSignInException.Kind.NoAccount ->
                "Google sign-in found no account for this app. Add a Google account to this phone. If you already have one, the app's Android OAuth client (package app.topgun.android and this build's signing SHA-1) is not registered in Google Cloud."
            GoogleSignInException.Kind.ProviderMissing ->
                "Google Play services is missing or out of date on this phone."
            GoogleSignInException.Kind.Other -> "Google sign-in failed (${e.message.orEmpty().take(80)})."
        }

        /** Google worked but the Top Gun server answered with an error. */
        fun serverMessage(e: ApiError): String = when (e) {
            is ApiError.Network -> "Signed in with Google, but the Top Gun server could not be reached. Check your connection."
            is ApiError.Unauthorized -> "The Top Gun server rejected your Google sign-in (token not accepted). The server must list this app's Google web client ID."
            is ApiError.Server -> "Signed in with Google, but the Top Gun server had an error creating your account (HTTP 500). This is a server problem."
            is ApiError.Validation -> "The Top Gun server refused the sign-in: ${e.userMessage}"
            else -> "Signed in with Google, but the Top Gun server returned an error: ${e.userMessage}"
        }

        const val GENERIC_ERROR = "Google sign-in didn't work. Check your connection and try again."
    }
}
