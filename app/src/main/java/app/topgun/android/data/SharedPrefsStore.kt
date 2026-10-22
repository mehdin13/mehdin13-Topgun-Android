// ANDROID-ONLY
package app.topgun.android.data

import android.content.Context
import app.topgun.android.platform.KeyValueStore

class SharedPrefsStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("topgun_prefs", Context.MODE_PRIVATE)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String?) = prefs.edit().apply { if (value == null) remove(key) else putString(key, value) }.apply()
    override fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    override fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
}
