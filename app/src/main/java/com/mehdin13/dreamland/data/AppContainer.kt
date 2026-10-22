package com.mehdin13.dreamland.data

import android.content.Context
import com.mehdin13.dreamland.BuildConfig
import com.mehdin13.dreamland.core.auth.SessionStore
import com.mehdin13.dreamland.core.network.ApiClient
import com.mehdin13.dreamland.core.network.ApiConfiguration
import com.mehdin13.dreamland.core.network.MediaUrlResolver
import com.mehdin13.dreamland.core.network.StoryApi
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/** Hand-wired dependencies, created once in [com.mehdin13.dreamland.DreamlandApp]. */
class AppContainer(context: Context) {
    val configuration = ApiConfiguration(BuildConfig.API_BASE_URL)
    val session = SessionStore(EncryptedTokenStore(context.applicationContext))
    val media = MediaUrlResolver(configuration)

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val client = ApiClient(configuration, session, http)
    val stories = StoryApi(client)
}
