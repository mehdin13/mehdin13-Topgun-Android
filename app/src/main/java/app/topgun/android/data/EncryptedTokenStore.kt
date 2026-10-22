// ANDROID-ONLY
package app.topgun.android.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.topgun.core.auth.TokenStore

/** API token in Keystore-backed encrypted preferences. Never logged or stored anywhere else. */
class EncryptedTokenStore(context: Context) : TokenStore {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "topgun_auth",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun read(): String? = prefs.getString(KEY, null)
    override fun save(token: String) = prefs.edit().putString(KEY, token).apply()
    override fun clear() = prefs.edit().remove(KEY).apply()

    private companion object { const val KEY = "token" }
}
