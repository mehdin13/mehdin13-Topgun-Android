// ANDROID-ONLY
package app.topgun.android.platform

import android.content.Context

/** TODO(port, details agent): downloaded 3D model files under cacheDir/models (iOS Model3DCache + CacheManager). */
class ModelCache(private val context: Context) {
    fun info(): ModelCacheInfo = ModelCacheInfo(0, 0L)
    fun clear() {}
}
