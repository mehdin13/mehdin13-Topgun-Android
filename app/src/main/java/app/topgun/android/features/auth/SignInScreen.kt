package app.topgun.android.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.BuildConfig
import app.topgun.android.R
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.UiText
import app.topgun.android.data.asString
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGEntry
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.components.SocialLoginButton

/** iOS `SignInView` shows the same placeholder address for both links (marked TODO there). */
private const val PRIVACY_POLICY_URL = "https://www.Topgunapp.com/"
private const val TERMS_URL = "https://www.Topgunapp.com/"

/**
 * Sign-in sheet (iOS `View/MainViews/SignInView.swift`). Android offers Google only; Apple needs a web flow.
 * On success the screen closes itself through [onDismiss]; a cancelled picker leaves the screen as it is.
 */
@Composable
fun SignInScreen(onDismiss: () -> Unit) {
    val container = LocalAppContainer.current
    val vm = containerViewModel { SignInViewModel(it.user) }
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.signedIn) { if (state.signedIn) onDismiss() }

    SignInContent(
        isLoading = state.isLoading,
        error = state.error,
        notice = state.notice,
        onGoogle = vm::signInWithGoogle,
        onClose = onDismiss,
        onOpenPrivacy = { container.platform.openUrl(PRIVACY_POLICY_URL) },
        onOpenTerms = { container.platform.openUrl(TERMS_URL) },
    )
}

@Composable
internal fun SignInContent(
    isLoading: Boolean,
    error: UiText?,
    notice: UiText? = null,
    onGoogle: () -> Unit,
    onClose: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(TG.colors.background).systemBarsPadding()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource(R.drawable.topgunlogo),
                contentDescription = stringResource(R.string.auth_logo_description),
                modifier = Modifier.padding(top = TGEntry.signInLogoTop).size(TGEntry.signInLogo),
            )
            Text(
                stringResource(R.string.auth_register_intro),
                style = TGEntry.signInBodyStyle.copy(lineHeight = TGEntry.signInBodyStyle.fontSize * 1.4f),
                color = TG.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = TGEntry.signInTextTop, start = TGEntry.signInTextHorizontal, end = TGEntry.signInTextHorizontal),
            )
            Spacer(Modifier.weight(1f))

            if (notice != null || error != null) {
                Text(
                    stringResource(R.string.auth_client_in_build, BuildConfig.GOOGLE_CLIENT_HINT),
                    style = TGEntry.footnoteStyle,
                    color = TG.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = TGSpace.xl),
                )
            }
            if (notice != null && error == null) {
                Text(
                    notice.asString(),
                    style = TGEntry.footnoteStyle,
                    color = TG.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = TGSpace.xl, vertical = TGSpace.s)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            if (error != null) {
                Text(
                    error.asString(),
                    style = TGEntry.footnoteStyle,
                    color = TGBrand.red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = TGSpace.xl, vertical = TGSpace.s)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            SocialLoginButton(
                icon = painterResource(R.drawable.google),
                text = stringResource(R.string.auth_sign_in_google),
                onClick = onGoogle,
                loading = isLoading,
                modifier = Modifier.padding(horizontal = TGSpace.l, vertical = TGSpace.xs / 2),
            )

            Row(
                Modifier.padding(bottom = TGEntry.signInBottom),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TGSpace.xs),
            ) {
                TextButton(onOpenPrivacy, Modifier.heightIn(min = TGSize.tapMin)) {
                    Text(stringResource(R.string.auth_privacy_policy), style = TGEntry.footnoteStyle, color = TG.colors.accent)
                }
                Text("|", style = TGEntry.footnoteStyle, color = TG.colors.textSecondary)
                TextButton(onOpenTerms, Modifier.heightIn(min = TGSize.tapMin)) {
                    Text(stringResource(R.string.auth_terms_of_service), style = TGEntry.footnoteStyle, color = TG.colors.accent)
                }
            }
        }
        // iOS presents this as a sheet that is swiped away; Android has a visible close button.
        Box(
            Modifier.align(Alignment.TopStart).size(TGSize.tapMin).clickable(onClickLabel = stringResource(R.string.auth_close), onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.auth_close), tint = TG.colors.textPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInPreview() {
    SignInContent(isLoading = false, error = null, onGoogle = {}, onClose = {}, onOpenPrivacy = {}, onOpenTerms = {})
}
