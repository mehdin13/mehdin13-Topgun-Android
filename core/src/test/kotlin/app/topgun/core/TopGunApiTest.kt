package app.topgun.core

import app.topgun.core.auth.InMemoryTokenStore
import app.topgun.core.auth.SessionStore
import app.topgun.core.model.AircraftFilter
import app.topgun.core.network.ApiClient
import app.topgun.core.network.ApiConfiguration
import app.topgun.core.network.ApiError
import app.topgun.core.network.TopGunApi
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy

class TopGunApiTest {
    private val server = MockWebServer().apply { start() }
    private val tokens = InMemoryTokenStore("secret-token")
    private val session = SessionStore(tokens)
    private val api = TopGunApi(ApiClient(ApiConfiguration(server.url("/api/v1/").toString()), session))

    @AfterTest fun tearDown() = server.shutdown()

    private fun enqueue(code: Int, body: String = "", headers: Map<String, String> = emptyMap()) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body).apply { headers.forEach { (k, v) -> addHeader(k, v) } })
    }

    private val emptyPage = """{"count":0,"next":null,"previous":null,"results":[]}"""

    @Test fun listsAircraftWithPagingAndTheToken() = runTest {
        enqueue(200, emptyPage)
        api.aircraft(page = 3, pageSize = 20)
        val request = server.takeRequest()
        assertEquals("Token secret-token", request.getHeader("Authorization"))
        assertEquals("/api/v1/aircrafts/aircraft/?page=3&page_size=20", request.path)
    }

    @Test fun anonymousUsersSendNoAuthorizationHeader() = runTest {
        val anon = TopGunApi(ApiClient(ApiConfiguration(server.url("/api/v1/").toString()), SessionStore(InMemoryTokenStore())))
        enqueue(200, emptyPage)
        anon.aircraft()
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test fun searchEncodesTheQuery() = runTest {
        enqueue(200, emptyPage)
        api.searchAircraft("F-35 & co")
        assertEquals("/api/v1/aircrafts/aircraft/aircraft_search/?aircraft_name=F-35%20%26%20co&page=1&page_size=10", server.takeRequest().path)
    }

    @Test fun filterPostsOnlyNonDefaultFormFields() = runTest {
        enqueue(200, emptyPage)
        api.filterAircraft(AircraftFilter(category = "Bomber", generations = listOf("4th", "5th")), page = 2)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/aircrafts/aircraft/aircraft_filters/?page=2&page_size=10", request.path)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("application/x-www-form-urlencoded"))
        assertEquals("category=Bomber&generation=4th%2C5th", request.body.readUtf8())
    }

    @Test fun getRetriesOnThrottlingButStopsAfterTwoRetries() = runTest {
        enqueue(429, """{"detail":"Request was throttled."}""", mapOf("Retry-After" to "0"))
        enqueue(429, "", mapOf("Retry-After" to "0"))
        enqueue(200, emptyPage)
        api.aircraft()
        assertEquals(3, server.requestCount)

        repeat(3) { enqueue(429, "", mapOf("Retry-After" to "0")) }
        assertFailsWith<ApiError.QuotaReached> { api.aircraft() }
        assertEquals(6, server.requestCount)
    }

    @Test fun writesAreNeverRetried() = runTest {
        enqueue(503)
        assertFailsWith<ApiError.ServiceBusy> { api.toggleLike(1) }
        assertEquals(1, server.requestCount)
    }

    @Test fun favoriteToggleReportsAddedOnCreatedAndRemovedOnOk() = runTest {
        enqueue(201, """{"message":"Added aircraft with id 5 into favorite list."}""")
        assertTrue(api.toggleFavorite(5))
        val request = server.takeRequest()
        assertEquals("/api/v1/users/favorites/", request.path)
        assertEquals("""{"aircraft_id":5}""", request.body.readUtf8())

        enqueue(200, """{"message":"Removed"}""")
        assertFalse(api.toggleFavorite(5))
    }

    @Test fun vipDetailMapsTo403VipRequired() = runTest {
        enqueue(403, """{"detail":"This aircraft is VIP and you do not have access."}""")
        val e = assertFailsWith<ApiError.Forbidden> { api.aircraftDetail(9) }
        assertTrue(e.isVipRequired)
    }

    @Test fun expiredTokenSignsTheSessionOut() = runTest {
        enqueue(401, """{"detail":"Invalid token"}""")
        assertFailsWith<ApiError.Unauthorized> { api.userInfo() }
        assertFalse(session.isSignedIn.value)
    }

    @Test fun socialLoginSendsNoTokenAndReadsTheReply() = runTest {
        enqueue(201, """{"message":"Login successful.","user_id":3,"token":"new-token","is_vip":false}""")
        val reply = api.socialLogin("google", "a@b.c", "Ann", null)
        assertEquals("new-token", reply.token)
        val request = server.takeRequest()
        assertNull(request.getHeader("Authorization"))
        assertEquals("""{"social_network":"google","email":"a@b.c","first_name":"Ann","last_name":""}""", request.body.readUtf8())
    }

    @Test fun commentsAndRatingsUseTheBackendFieldNames() = runTest {
        enqueue(201, """{"message":"Created New Comment"}""")
        api.postComment(aircraftId = 4, body = "Nice", parentId = 2)
        assertEquals("""{"aircraft":4,"body":"Nice","parent":2}""", server.takeRequest().body.readUtf8())

        assertFailsWith<IllegalArgumentException> { api.rate(4, 0) }
        enqueue(200, """{"message":"Vote recorded.","rating":5,"average_rating":4.6}""")
        assertEquals(4.6, api.rate(4, 5).averageRating)
        assertEquals("""{"rating":5}""", server.takeRequest().body.readUtf8().also { /* consumed in order */ })
    }

    @Test fun offlineMapsToNetwork() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertFailsWith<ApiError.Network> { api.categories() }
    }

    @Test fun malformedBodyMapsToDecoding() = runTest {
        enqueue(200, """{"results":"nope"}""")
        assertFailsWith<ApiError.Decoding> { api.aircraft() }
    }
}
