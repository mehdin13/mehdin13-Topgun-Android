package app.topgun.android.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.topgun.android.data.UserRepository
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
            } catch (e: ApiError) {
                _state.update { SignInState(error = e.userMessage) }
            } catch (e: Exception) {
                // Credential Manager / Play services failure: nothing useful to show a user, and no details are logged.
                _state.update { SignInState(error = GENERIC_ERROR) }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    companion object {
        const val GENERIC_ERROR = "Google sign-in didn't work. Check your connection and try again."
    }
}
