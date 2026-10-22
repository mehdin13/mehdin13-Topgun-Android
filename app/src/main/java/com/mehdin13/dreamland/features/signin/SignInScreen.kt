package com.mehdin13.dreamland.features.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehdin13.dreamland.BuildConfig
import com.mehdin13.dreamland.core.auth.SessionStore
import com.mehdin13.dreamland.core.network.StoryApi
import com.mehdin13.dreamland.data.asApiError
import com.mehdin13.dreamland.data.containerViewModel
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLLayout
import com.mehdin13.dreamland.designsystem.DLSpacing
import com.mehdin13.dreamland.designsystem.components.DLButton
import com.mehdin13.dreamland.designsystem.components.DLButtonVariant
import com.mehdin13.dreamland.designsystem.components.ErrorView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignInState(val isWorking: Boolean = false, val message: String? = null)

class SignInViewModel(private val api: StoryApi, private val session: SessionStore) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    /** Google/Apple exchange needs `/api/users/*`, which is not mounted yet (BACKEND_GAPS #1, #12). */
    fun socialSignInUnavailable() {
        _state.value = SignInState(message = "Sign-in isn't switched on yet. We're finishing it up.")
    }

    /** DEBUG builds only: checks a pasted test-account token against the API before saving it. Never logged. */
    fun devSignIn(token: String) {
        if (!BuildConfig.DEBUG || token.isBlank()) return
        viewModelScope.launch {
            _state.value = SignInState(isWorking = true)
            try {
                api.verifyToken(token.trim())
                session.signIn(token.trim())
                _state.value = SignInState()
            } catch (e: Throwable) {
                _state.value = SignInState(message = e.asApiError().userMessage)
            }
        }
    }
}

@Composable
fun SignInScreen() {
    val vm = containerViewModel { SignInViewModel(it.stories, it.session) }
    val state by vm.state.collectAsStateWithLifecycle()
    var devToken by remember { mutableStateOf("") }
    val c = DL.colors

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DLLayout.screenMarginCompact, vertical = DLSpacing.space32),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(DLSpacing.space48))
        Text("Dreamland", style = DL.type.displayLarge, color = c.textPrimary, textAlign = TextAlign.Center)
        Text(
            "Your child's ideas and paintings become illustrated stories.",
            style = DL.type.bodyLarge,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(DLSpacing.space32))

        // Sign in with Google and Apple only. No passwords, no forgot-password flow.
        // TODO(session 2): Credential Manager (Google) and a web flow for Apple once the backend exchange exists.
        DLButton("Continue with Google", onClick = vm::socialSignInUnavailable, fullWidth = true)
        DLButton("Continue with Apple", onClick = vm::socialSignInUnavailable, fullWidth = true, variant = DLButtonVariant.Secondary)

        state.message?.let { ErrorView(it) }

        if (BuildConfig.DEBUG) {
            Spacer(Modifier.height(DLSpacing.space24))
            Text("Developer sign-in (debug builds only)", style = DL.type.metadataLabel, color = c.textTertiary)
            OutlinedTextField(
                value = devToken,
                onValueChange = { devToken = it },
                label = { Text("Test-account token") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            DLButton(
                "Sign in with token",
                onClick = { vm.devSignIn(devToken) },
                fullWidth = true,
                variant = DLButtonVariant.Tertiary,
                enabled = devToken.isNotBlank(),
                isLoading = state.isWorking,
            )
        }
    }
}
