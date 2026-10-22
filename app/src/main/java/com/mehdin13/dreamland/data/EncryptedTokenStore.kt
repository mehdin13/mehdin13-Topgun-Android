package com.mehdin13.dreamland.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mehdin13.dreamland.core.auth.TokenStore

/** Auth token in Keystore-backed encrypted preferences. The token is never logged or written anywhere else. */
class EncryptedTokenStore(context: Context) : TokenStore {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "dreamland_auth",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun read(): String? = prefs.getString(KEY, null)
    override fun save(token: String) = prefs.edit().putString(KEY, token).apply()
    override fun clear() = prefs.edit().remove(KEY).apply()

    private companion object { const val KEY = "token" }
}
