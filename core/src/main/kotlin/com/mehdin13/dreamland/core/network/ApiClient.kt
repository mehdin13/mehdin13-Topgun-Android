package com.mehdin13.dreamland.core.network

import com.mehdin13.dreamland.core.auth.SessionStore
import com.mehdin13.dreamland.core.model.DreamlandJson
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

/** The network surface the rest of the app depends on. Tests inject a fake conforming to this. */
interface ApiClientContract {
    suspend fun <T> send(endpoint: ApiEndpoint, deserializer: DeserializationStrategy<T>): T
    suspend fun send(endpoint: ApiEndpoint)
}

/**
 * OkHttp + kotlinx-serialization client. Adds the token header, maps every failure to [ApiError],
 * and signs the session out on a 401 for authenticated requests.
 * Never retries: generation POSTs spend coins, so a retry could charge twice.
 */
class ApiClient(
    private val configuration: ApiConfiguration,
    private val session: SessionStore,
    private val http: OkHttpClient = OkHttpClient(),
) : ApiClientContract {

    override suspend fun <T> send(endpoint: ApiEndpoint, deserializer: DeserializationStrategy<T>): T {
        val body = perform(endpoint)
        return try {
            DreamlandJson.decodeFromString(deserializer, body)
        } catch (e: kotlinx.serialization.SerializationException) {
            throw ApiError.Decoding
        } catch (e: IllegalArgumentException) {
            throw ApiError.Decoding
        }
    }

    override suspend fun send(endpoint: ApiEndpoint) {
        perform(endpoint)
    }

    private suspend fun perform(endpoint: ApiEndpoint): String {
        val request = makeRequest(endpoint)
        val client = endpoint.readTimeoutSeconds
            ?.let { http.newBuilder().readTimeout(it, TimeUnit.SECONDS).writeTimeout(it, TimeUnit.SECONDS).build() }
            ?: http

        val response = try {
            client.newCall(request).await()
        } catch (e: IOException) {
            throw ApiError.Network
        }

        response.use {
            val text = it.body?.string().orEmpty()
            if (it.isSuccessful) return text

            val error = ApiError.from(it.code, text, it.header("Retry-After"))
            if (error is ApiError.Unauthorized && endpoint.requiresAuth) session.signOut()
            throw error
        }
    }

    private fun makeRequest(endpoint: ApiEndpoint): Request {
        val url = configuration.baseUrl.newBuilder().apply {
            // Resolve relative to the API root, keeping trailing slashes DRF expects.
            val segments = endpoint.path.split("/")
            segments.forEachIndexed { index, segment ->
                if (segment.isEmpty() && index == segments.lastIndex) addPathSegment("") else if (segment.isNotEmpty()) addPathSegment(segment)
            }
            endpoint.query.forEach { (k, v) -> addQueryParameter(k, v) }
        }.build()

        val builder = Request.Builder().url(url).header("Accept", "application/json")
        if (endpoint.requiresAuth) session.token()?.let { builder.header("Authorization", "Token $it") }

        val body: RequestBody? = when (val b = endpoint.body) {
            ApiEndpoint.Body.None -> null
            is ApiEndpoint.Body.Json -> b.text.toRequestBody(JSON)
            is ApiEndpoint.Body.Multipart -> b.body.withProgress(endpoint.onUploadProgress)
            is ApiEndpoint.Body.Raw -> b.body
        }
        // OkHttp requires a body for POST/PATCH.
        val method = endpoint.method.name
        val effectiveBody = body ?: if (method == "POST" || method == "PATCH") ByteArray(0).toRequestBody(null) else null
        return builder.method(method, effectiveBody).build()
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) = cont.resume(response)
        override fun onFailure(call: Call, e: IOException) {
            if (!cont.isCancelled) cont.resumeWithException(e)
        }
    })
    cont.invokeOnCancellation { runCatching { cancel() } }
}

private fun RequestBody.withProgress(onProgress: ((Double) -> Unit)?): RequestBody {
    if (onProgress == null) return this
    val delegate = this
    return object : RequestBody() {
        override fun contentType() = delegate.contentType()
        override fun contentLength() = delegate.contentLength()
        override fun writeTo(sink: BufferedSink) {
            val total = contentLength().toDouble()
            var written = 0L
            val counting = object : ForwardingSink(sink) {
                override fun write(source: Buffer, byteCount: Long) {
                    super.write(source, byteCount)
                    written += byteCount
                    if (total > 0) onProgress((written / total).coerceIn(0.0, 1.0))
                }
            }.buffer()
            delegate.writeTo(counting)
            counting.flush()
        }
    }
}
