package app.topgun.android.data

import app.topgun.core.network.ApiError

/** What a screen shows while a request is loading, done, or failed. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Loaded<T>(val value: T) : Load<T>
    data class Failed(val error: ApiError) : Load<Nothing>
}

/** Turns any thrown error into an [ApiError] so views only ever show friendly messages. */
fun Throwable.asApiError(): ApiError = when (this) {
    is ApiError -> this
    is java.io.IOException -> ApiError.Network
    else -> ApiError.UnexpectedStatus(0)
}

/** Runs [block] and wraps the outcome; cancellation is rethrown so scopes still cancel. */
suspend fun <T> loading(block: suspend () -> T): Load<T> = try {
    Load.Loaded(block())
} catch (e: kotlin.coroutines.cancellation.CancellationException) {
    throw e
} catch (e: Throwable) {
    Load.Failed(e.asApiError())
}
