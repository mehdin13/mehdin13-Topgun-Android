// ANDROID-ONLY
package app.topgun.android.platform

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import app.topgun.android.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.lang.ref.WeakReference

/**
 * Google sign-in through Credential Manager (`GetGoogleIdOption`). The server client id is the OAuth *web* client
 * (see docs/SETUP.md). Credential Manager needs an Activity to show its sheet, so the helper remembers the
 * resumed Activity of the app; [signIn] therefore only works while the UI is in the foreground.
 *
 * The backend login (`users/social_network_register/`) takes only the e-mail and names, so the ID token is not
 * sent anywhere (see docs/BACKEND_GAPS.md #1/#19); it is only parsed to read the account.
 */
class GoogleSignInHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    init {
        // Track the foreground Activity (the helper is created in Application.onCreate, before any Activity exists).
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(ForegroundActivity)
    }

    /** Returns the chosen account, or null when the user cancelled. Throws on any other failure. */
    suspend fun signIn(): GoogleAccount? {
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        check(clientId.isNotBlank()) { "topgun.googleWebClientId is not set in local.properties" }
        val activity = ForegroundActivity.current() ?: unwrapActivity(context)
            ?: error("No foreground Activity to show the Google sign-in sheet")

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false) // offer every Google account on the device, also first-time ones
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val result = try {
            credentialManager.getCredential(activity, request)
        } catch (e: GetCredentialCancellationException) {
            return null
        }

        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val google = try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (e: GoogleIdTokenParsingException) {
                throw IllegalStateException("Unreadable Google credential", e)
            }
            return GoogleAccount(
                email = google.id, // for Google ID credentials `id` is the e-mail address
                firstName = google.givenName,
                lastName = google.familyName,
                photoUrl = google.profilePictureUri?.toString(),
            )
        }
        throw IllegalStateException("Unexpected credential type")
    }

    /** Forgets the chosen account so the next sign-in shows the picker again. */
    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    private fun unwrapActivity(start: Context): Activity? {
        var c: Context? = start
        while (c is ContextWrapper) {
            if (c is Activity) return c
            c = c.baseContext
        }
        return null
    }

    /** Remembers the last resumed Activity without holding it alive. */
    private object ForegroundActivity : Application.ActivityLifecycleCallbacks {
        private var ref: WeakReference<Activity>? = null
        fun current(): Activity? = ref?.get()?.takeIf { !it.isFinishing && !it.isDestroyed }
        override fun onActivityResumed(activity: Activity) { ref = WeakReference(activity) }
        override fun onActivityPaused(activity: Activity) { if (ref?.get() === activity) ref = null }
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}
