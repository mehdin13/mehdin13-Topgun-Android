package app.topgun.android.data

import app.topgun.android.platform.PlatformServices
import app.topgun.android.platform.KeyValueStore
import app.topgun.core.auth.SessionStore
import app.topgun.core.auth.TokenStore
import app.topgun.core.network.ApiClient
import app.topgun.core.network.ApiConfiguration
import app.topgun.core.network.MediaUrlResolver
import app.topgun.core.network.TopGunApi
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Hand-wired dependencies. Pure Kotlin so it can be type-checked off-device; Android pieces come in as parameters. */
class AppContainer(
    apiBaseUrl: String,
    tokenStore: TokenStore,
    keyValueStore: KeyValueStore,
    val platform: PlatformServices,
) {
    val configuration = ApiConfiguration(apiBaseUrl)
    val session = SessionStore(tokenStore)
    val media = MediaUrlResolver(configuration)
    val prefs = AppPreferences(keyValueStore)

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api = TopGunApi(ApiClient(configuration, session, http))
    val user = UserRepository(api, session, prefs, platform)
}
