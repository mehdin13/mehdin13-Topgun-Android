package com.mehdin13.dreamland.core

import com.mehdin13.dreamland.core.auth.InMemoryTokenStore
import com.mehdin13.dreamland.core.auth.SessionStore
import com.mehdin13.dreamland.core.model.StoryDto
import com.mehdin13.dreamland.core.network.ApiClient
import com.mehdin13.dreamland.core.network.ApiConfiguration
import com.mehdin13.dreamland.core.network.ApiError
import com.mehdin13.dreamland.core.network.ApiEndpoint
import com.mehdin13.dreamland.core.network.FeedSort
import com.mehdin13.dreamland.core.network.StoryApi
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy

class ApiClientTest {
    private val server = MockWebServer().apply { start() }
    private val tokens = InMemoryTokenStore("secret-token")
    private val session = SessionStore(tokens)
    private val client = ApiClient(ApiConfiguration(server.url("/api/").toString()), session)
    private val api = StoryApi(client)

    @AfterTest fun tearDown() = server.shutdown()

    private fun enqueue(code: Int, body: String = "", headers: Map<String, String> = emptyMap()) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body).apply { headers.forEach { (k, v) -> addHeader(k, v) } })
    }

    @Test fun sendsTokenHeaderAndKeepsTrailingSlash() = runTest {
        enqueue(200, "[]")
        api.myStories()
        val request = server.takeRequest()
        assertEquals("Token secret-token", request.getHeader("Authorization"))
        assertEquals("/api/stories/my_stories/", request.path)
    }

    @Test fun verifyTokenUsesTheGivenTokenAndLeavesTheSessionAlone() = runTest {
        enqueue(401)
        assertFailsWith<ApiError.Unauthorized> { api.verifyToken("candidate") }
        assertEquals("Token candidate", server.takeRequest().getHeader("Authorization"))
        assertTrue(session.isSignedIn.value)
        assertEquals("secret-token", tokens.read())
    }

    @Test fun publicEndpointsSendNoToken() = runTest {
        enqueue(200, "[]")
        api.publicStories(sort = FeedSort.POPULAR, category = "Fairy tale", search = "dragon")
        val request = server.takeRequest()
        assertNull(request.getHeader("Authorization"))
        assertEquals("/api/stories/public/?sort=popular&category=Fairy%20tale&search=dragon", request.path)
    }

    @Test fun unauthorizedSignsOutOnAuthenticatedRequests() = runTest {
        enqueue(401, """{"detail":"Invalid token."}""")
        assertFailsWith<ApiError.Unauthorized> { api.myStories() }
        assertFalse(session.isSignedIn.value)
        assertNull(tokens.read())
    }

    @Test fun unauthorizedOnPublicRequestKeepsSession() = runTest {
        enqueue(401)
        assertFailsWith<ApiError.Unauthorized> { api.publicStories() }
        assertTrue(session.isSignedIn.value)
    }

    @Test fun mapsQuotaAndBusyWithRetryAfter() = runTest {
        enqueue(429, """{"detail":"Daily limit reached."}""", mapOf("Retry-After" to "60"))
        val quota = assertFailsWith<ApiError.QuotaReached> { api.quota() }
        assertEquals(60L, quota.retryAfterSeconds)
        assertEquals("Daily limit reached.", quota.userMessage)

        enqueue(503)
        assertFailsWith<ApiError.ServiceBusy> { api.quota() }
    }

    @Test fun networkFailureMapsToNetwork() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertFailsWith<ApiError.Network> { api.myStories() }
    }

    @Test fun malformedBodyMapsToDecoding() = runTest {
        enqueue(200, """{"nope":true}""")
        assertFailsWith<ApiError.Decoding> { api.story(1) }
    }

    @Test fun ratingIsValidatedAndSentAsSnakeCaseJson() = runTest {
        assertFailsWith<IllegalArgumentException> { api.rate(1, 6) }
        enqueue(200, "{}")
        api.rate(9, 5)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/stories/9/rate/", request.path)
        assertEquals("""{"score":5}""", request.body.readUtf8())
    }

    // BACKEND_GAPS #47: the backend sends a JSON body with its 204. OkHttp rejects that as a protocol error
    // (it would surface as ApiError.Network), so this only covers the compliant, empty 204.
    @Test fun unsaveAcceptsAnEmptyNoContent() = runTest {
        enqueue(204)
        api.unsave(3)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test fun favoriteDecodesServerCount() = runTest {
        enqueue(200, """{"favorited":true,"story_id":4,"favorite_count":12}""")
        val r = api.favorite(4)
        assertTrue(r.favorited)
        assertEquals(12, r.favoriteCount)
    }

    @Test fun multipartUploadReportsProgress() = runTest {
        enqueue(201, """{"id":1,"created_at":"2026-10-11T10:00:00Z","updated_at":"2026-10-11T10:00:00Z"}""")
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("theme", "adventure")
            .addFormDataPart("image", "p.jpg", ByteArray(4096).toRequestBody("image/jpeg".toMediaType()))
            .build()
        val progress = mutableListOf<Double>()
        client.send(ApiEndpoint.multipart("stories/from-image/", body, onUploadProgress = { progress += it }), StoryDto.serializer())
        val request = server.takeRequest()
        assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        assertTrue(progress.isNotEmpty() && progress.last() == 1.0)
    }
}
