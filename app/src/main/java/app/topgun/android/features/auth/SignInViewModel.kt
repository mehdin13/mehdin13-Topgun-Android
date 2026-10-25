package app.topgun.android.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.R
import app.topgun.android.data.UiText
import app.topgun.android.data.UserRepository
import app.topgun.android.data.localized
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
    val error: UiText? = null,
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
        fun googleMessage(e: GoogleSignInException): UiText = when (e.kind) {
            GoogleSignInException.Kind.NotConfigured -> UiText.Res(R.string.auth_error_google_not_configured)
            GoogleSignInException.Kind.NoAccount -> UiText.Res(R.string.auth_error_google_no_account)
            GoogleSignInException.Kind.ProviderMissing -> UiText.Res(R.string.auth_error_google_provider_missing)
            GoogleSignInException.Kind.Other -> UiText.Res(R.string.auth_error_google_failed, e.message.orEmpty().take(80))
        }

        /** Google worked but the Top Gun server answered with an error. */
        fun serverMessage(e: ApiError): UiText = when (e) {
            is ApiError.Network -> UiText.Res(R.string.auth_error_server_unreachable)
            is ApiError.Unauthorized -> UiText.Res(R.string.auth_error_server_rejected)
            is ApiError.Server -> UiText.Res(R.string.auth_error_server_500)
            is ApiError.Validation -> UiText.Res(R.string.auth_error_server_refused, e.localized())
            else -> UiText.Res(R.string.auth_error_server_other, e.localized())
        }

        val GENERIC_ERROR: UiText = UiText.Res(R.string.auth_error_generic)
    }
}
