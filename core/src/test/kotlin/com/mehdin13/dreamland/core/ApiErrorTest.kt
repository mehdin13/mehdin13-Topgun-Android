package com.mehdin13.dreamland.core

import com.mehdin13.dreamland.core.network.ApiError
import com.mehdin13.dreamland.core.network.DrfErrorMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiErrorTest {
    @Test fun mapsEachStatusToItsCase() {
        assertEquals(ApiError.Validation("bad"), ApiError.from(400, """{"detail":"bad"}""", null))
        assertEquals(ApiError.Unauthorized, ApiError.from(401, "", null))
        assertEquals(ApiError.InsufficientCoins("no coins"), ApiError.from(402, """{"error":"no coins"}""", null))
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

    @Test fun parsesDrfShapesDeterministically() {
        assertEquals("d", DrfErrorMessage.parse("""{"detail":"d","error":"e"}"""))
        assertEquals("e", DrfErrorMessage.parse("""{"error":"e"}"""))
        assertEquals("a-first", DrfErrorMessage.parse("""{"b":["b-first"],"a":["a-first","a-second"]}"""))
        assertNull(DrfErrorMessage.parse(""))
        assertNull(DrfErrorMessage.parse("not json"))
        assertNull(DrfErrorMessage.parse("[1,2]"))
    }

    @Test fun userMessagesNeverLeakTransportDetails() {
        assertEquals("You appear to be offline. Check your connection and try again.", ApiError.Network.userMessage)
        assertEquals("The story service is temporarily busy. Please try again.", ApiError.ServiceBusy(null).userMessage)
        assertEquals("Something went wrong. Please try again.", ApiError.UnexpectedStatus(418).userMessage)
    }
}
