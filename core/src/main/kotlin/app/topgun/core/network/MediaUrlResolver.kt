package app.topgun.core.network

import okhttp3.HttpUrl

/**
 * Turns the API's media fields into loadable URLs. Django sends relative paths such as
 * `/media/stories/covers/x.png`; those resolve against the server origin. Absolute http(s) URLs pass through.
 */
class MediaUrlResolver(private val origin: HttpUrl) {
    constructor(configuration: ApiConfiguration) : this(configuration.origin)

    /** Returns null for missing or empty values (media is often null while generation is incomplete). */
    fun resolve(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null

        val scheme = SCHEME.find(value)?.groupValues?.get(1)?.lowercase()
        if (scheme != null) {
            return if (scheme == "http" || scheme == "https") value else null
        }
        return origin.resolve(value)?.toString()
    }

    private companion object {
        val SCHEME = Regex("^([a-zA-Z][a-zA-Z0-9+.-]*):")
    }
}
