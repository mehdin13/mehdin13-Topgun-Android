package app.topgun.core.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * The API root, for example `https://host/api/`. A trailing slash is added when missing.
 * The base URL is injected (BuildConfig in the app, a mock server in tests); it is never hardcoded in source.
 */
class ApiConfiguration(baseUrl: String) {
    val baseUrl: HttpUrl = baseUrl.let { if (it.endsWith("/")) it else "$it/" }.toHttpUrl()

    /** Server origin (scheme + host + port), used to resolve relative media paths. */
    val origin: HttpUrl = this.baseUrl.newBuilder().encodedPath("/").query(null).fragment(null).build()
}
