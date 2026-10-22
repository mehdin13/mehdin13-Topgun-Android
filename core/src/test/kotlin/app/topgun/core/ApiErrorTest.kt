package app.topgun.core

import app.topgun.core.network.ApiError
import app.topgun.core.network.DrfErrorMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiErrorTest {
    @Test fun mapsEachStatusToItsCase() {
        assertEquals(ApiError.Validation("bad"), ApiError.from(400, """{"detail":"bad"}""", null))
        assertEquals(ApiError.Unauthorized, ApiError.from(401, "", null))
        assertEquals(ApiError.Forbidden(null), ApiError.from(403, "", null))
        assertEquals(ApiError.NotFound, ApiError.from(404, "", null))
        assertEquals(ApiError.Conflict(null), ApiError.from(409, "", null))
        assertEquals(ApiError.PayloadTooLarge, ApiError.from(413, "", null))
        assertEquals(ApiError.Server(null), ApiError.from(500, "", null))
        assertEquals(ApiError.BadGateway, ApiError.from(502, "", null))
        assertEquals(ApiError.UnexpectedStatus(418), ApiError.from(418, "", null))
    }

    @Test fun readsRetryAfterOn429And503() {
        assertEquals(30L, ApiError.from(429, "", "30").retryAfter)
        assertEquals(5L, ApiError.from(503, "", " 5 ").retryAfter)
        assertNull(ApiError.from(503, "", "Wed, 21 Oct 2026 07:28:00 GMT").retryAfter)
        assertNull(ApiError.from(404, "", "30").retryAfter)
    }

    @Test fun parsesTheApisErrorShapes() {
        assertEquals("d", DrfErrorMessage.parse("""{"detail":"d","error":"e"}"""))
        assertEquals("e", DrfErrorMessage.parse("""{"error":"e","message":"m"}"""))
        assertEquals("m", DrfErrorMessage.parse("""{"message":"m"}"""))
        assertEquals("a-first", DrfErrorMessage.parse("""{"b":["b-first"],"a":["a-first","a-second"]}"""))
        assertNull(DrfErrorMessage.parse(""))
        assertNull(DrfErrorMessage.parse("not json"))
        assertNull(DrfErrorMessage.parse("[1,2]"))
    }

    @Test fun vipForbiddenIsRecognisedFromTheServerMessage() {
        val vip = ApiError.from(403, """{"detail":"This aircraft is VIP and you do not have access."}""", null) as ApiError.Forbidden
        assertTrue(vip.isVipRequired)
        assertEquals("This aircraft is for Top Gun Pro members.", vip.userMessage)
        assertFalse((ApiError.from(403, """{"detail":"Nope"}""", null) as ApiError.Forbidden).isVipRequired)
    }

    @Test fun userMessagesNeverLeakTransportDetails() {
        assertEquals("You appear to be offline. Check your connection and try again.", ApiError.Network.userMessage)
        assertEquals("The server is temporarily busy. Please try again.", ApiError.ServiceBusy(null).userMessage)
        assertEquals("Something went wrong. Please try again.", ApiError.UnexpectedStatus(418).userMessage)
    }
}
