package com.mehdin13.dreamland.core.network

import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * One request against the Dreamland API. [path] is relative to the API root (no leading slash),
 * for example `stories/` or `stories/12/favorite/`. DRF expects trailing slashes.
 */
class ApiEndpoint(
    val method: Method,
    val path: String,
    val query: List<Pair<String, String>> = emptyList(),
    val body: Body = Body.None,
    /** Set false only for public endpoints (Discover, shared links). Everything else sends the token. */
    val requiresAuth: Boolean = true,
    /** Story generation runs inside the request on the backend (BACKEND_GAPS #9), so it needs a long read timeout. */
    val readTimeoutSeconds: Long? = null,
    /** Called with the fraction (0..1) of the body sent. Only used for uploads. */
    val onUploadProgress: ((Double) -> Unit)? = null,
) {
    enum class Method { GET, POST, PATCH, DELETE }

    sealed interface Body {
        data object None : Body
        data class Json(val text: String) : Body
        data class Multipart(val body: MultipartBody) : Body
        data class Raw(val body: RequestBody) : Body
    }

    companion object {
        fun get(path: String, query: List<Pair<String, String>> = emptyList(), requiresAuth: Boolean = true) =
            ApiEndpoint(Method.GET, path, query, requiresAuth = requiresAuth)

        fun delete(path: String) = ApiEndpoint(Method.DELETE, path)

        fun post(path: String) = ApiEndpoint(Method.POST, path)

        fun postJson(path: String, json: String, requiresAuth: Boolean = true) =
            ApiEndpoint(Method.POST, path, body = Body.Json(json), requiresAuth = requiresAuth)

        fun multipart(
            path: String,
            body: MultipartBody,
            readTimeoutSeconds: Long? = null,
            onUploadProgress: ((Double) -> Unit)? = null,
        ) = ApiEndpoint(Method.POST, path, body = Body.Multipart(body), readTimeoutSeconds = readTimeoutSeconds, onUploadProgress = onUploadProgress)
    }
}
