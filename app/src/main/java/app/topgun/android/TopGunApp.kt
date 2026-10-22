// ANDROID-ONLY
package app.topgun.android

import android.app.Application
import app.topgun.android.data.AppContainer
import app.topgun.android.data.EncryptedTokenStore
import app.topgun.android.data.SharedPrefsStore
import app.topgun.android.platform.AndroidPlatform
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder

class TopGunApp : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(
            apiBaseUrl = BuildConfig.API_BASE_URL,
            tokenStore = EncryptedTokenStore(this),
            keyValueStore = SharedPrefsStore(this),
            platform = AndroidPlatform(this),
        )
    }

    /** Backend flag and category icons can be SVG. */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components { add(SvgDecoder.Factory()) }
        .crossfade(true)
        .build()
}
