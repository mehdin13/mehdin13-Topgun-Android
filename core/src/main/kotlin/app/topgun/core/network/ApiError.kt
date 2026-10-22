package app.topgun.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Every failure the app shows or branches on. HTTP statuses map 1:1 to a case. */
sealed class ApiError(message: String? = null) : Exception(message) {
    /** 400 or any other validation response, with the server's field or detail message when present. */
    data class Validation(val serverMessage: String?) : ApiError(serverMessage)

    /** 401: the token is no longer valid. The session has already been cleared. */
    data object Unauthorized : ApiError()

    /** 403: for example a VIP-only aircraft opened without a subscription. */
    data class Forbidden(val serverMessage: String?) : ApiError(serverMessage) {
        val isVipRequired: Boolean get() = serverMessage?.contains("VIP", ignoreCase = true) == true
    }
    data object NotFound : ApiError()
    data class Conflict(val serverMessage: String?) : ApiError(serverMessage)
    data object PayloadTooLarge : ApiError()

    /** 429: the API allows about 4 requests per second per user. */
    data class QuotaReached(val retryAfterSeconds: Long?, val serverMessage: String?) : ApiError(serverMessage)

    data class Server(val serverMessage: String?) : ApiError(serverMessage)
    data object BadGateway : ApiError()

    /** 503: the server is busy; show a retry with backoff. */
    data class ServiceBusy(val retryAfterSeconds: Long?) : ApiError()

    data class UnexpectedStatus(val code: Int) : ApiError("HTTP $code")

    /** The request never got a usable response (offline, timeout, DNS). */
    data object Network : ApiError()

    /** The response body did not match the expected DTO. */
    data object Decoding : ApiError()

    /** The endpoint could not be turned into a URL. */
    data object InvalidRequest : ApiError()

    /** Friendly text for a child or parent. Never shows raw server or transport details. */
    val userMessage: String
        get() = when (this) {
            is Validation -> serverMessage ?: "Something about that request isn't right. Check it and try again."
            is Conflict -> serverMessage ?: "Something about that request isn't right. Check it and try again."
            is Forbidden -> if (isVipRequired) "This aircraft is for Top Gun Pro members." else serverMessage ?: "You don't have access to that."
            Unauthorized -> "Your session has ended. Please sign in again."
            NotFound -> "We couldn't find that. It may have been removed."
            PayloadTooLarge -> "That file is too large."
            is QuotaReached -> serverMessage ?: "Too many requests. Please wait a moment and try again."
            is Server -> "Something went wrong on our side. Please try again."
            BadGateway -> "The server couldn't be reached. Please try again."
            is ServiceBusy -> "The server is temporarily busy. Please try again."
            is UnexpectedStatus, Decoding, InvalidRequest -> "Something went wrong. Please try again."
            Network -> "You appear to be offline. Check your connection and try again."
        }

    /** Seconds to wait before retrying, when the server said so. */
    val retryAfter: Long?
        get() = when (this) {
            is QuotaReached -> retryAfterSeconds
            is ServiceBusy -> retryAfterSeconds
            else -> null
        }

    companion object {
        /** Maps an HTTP response to an error. [body] is the raw DRF error payload, if any. */
        fun from(status: Int, body: String, retryAfterHeader: String?): ApiError {
            val message = DrfErrorMessage.parse(body)
            val retryAfter = retryAfterHeader?.trim()?.toLongOrNull()
            return when (status) {
                400 -> Validation(message)
                401 -> Unauthorized
                403 -> Forbidden(message)
                404 -> NotFound
                409 -> Conflict(message)
                413 -> PayloadTooLarge
                429 -> QuotaReached(retryAfter, message)
                500 -> Server(message)
                502 -> BadGateway
                503 -> ServiceBusy(retryAfter)
                else -> UnexpectedStatus(status)
            }
        }
    }
}

/** Reads the API's error shapes: `{"detail": "..."}`, `{"error": "..."}`, `{"message": "..."}`, `{"field": ["..."]}`. */
object DrfErrorMessage {
    fun parse(body: String): String? {
        if (body.isBlank()) return null
        val obj = runCatching { Json.parseToJsonElement(body) }.getOrNull() as? JsonObject ?: return null

        (obj["detail"] as? JsonPrimitive)?.contentOrNull?.let { return it }
        (obj["error"] as? JsonPrimitive)?.contentOrNull?.let { return it }
        (obj["message"] as? JsonPrimitive)?.contentOrNull?.let { return it }

        // Field errors: take the first message in key order so the result is deterministic.
        for (key in obj.keys.sorted()) {
            val first = (obj[key] as? JsonArray)?.firstOrNull() as? JsonPrimitive
            first?.contentOrNull?.let { return it }
        }
        return null
    }
}
