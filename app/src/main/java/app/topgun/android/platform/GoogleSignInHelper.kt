// ANDROID-ONLY
package app.topgun.android.platform

import android.content.Context

/** TODO(port, auth agent): Credential Manager + GetGoogleIdOption (server client id = BuildConfig.GOOGLE_WEB_CLIENT_ID). */
class GoogleSignInHelper(private val context: Context) {
    suspend fun signIn(): GoogleAccount? = throw UnsupportedOperationException("Google sign-in is not implemented yet")
    suspend fun signOut() {}
}
