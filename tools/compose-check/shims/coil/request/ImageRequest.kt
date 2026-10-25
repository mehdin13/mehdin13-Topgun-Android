package coil.request

import android.content.Context

/** Shim of Coil 2.x `ImageRequest` (only what the checked code calls). */
class ImageRequest private constructor() {
    class Builder(context: Context) {
        fun data(data: Any?): Builder = this
        fun setParameter(key: String, value: Any?, memoryCacheKey: String? = value?.toString()): Builder = this
        fun crossfade(enable: Boolean): Builder = this
        fun build(): ImageRequest = ImageRequest()
    }
}
