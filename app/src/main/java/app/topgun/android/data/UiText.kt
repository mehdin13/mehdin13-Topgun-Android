package app.topgun.android.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.topgun.android.R
import app.topgun.core.network.ApiError

/**
 * Text a ViewModel can hold without a Context: either text that must stay as it is (backend messages) or a string
 * resource id with format arguments. Arguments may themselves be [UiText]. Resolved at the edge with [asString].
 */
sealed interface UiText {
    /** Text that is shown unchanged (for example a message written by the backend). */
    data class Raw(val value: String) : UiText

    /** A string resource ([id] is an `R.string` id) with `%1$s`-style [args]. */
    class Res(val id: Int, val args: List<Any>) : UiText {
        constructor(id: Int, vararg args: Any) : this(id, args.toList())
        override fun equals(other: Any?) = other is Res && other.id == id && other.args == args
        override fun hashCode() = 31 * id + args.hashCode()
    }
}

fun UiText.asString(context: Context): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> {
        val resolved = args.map { if (it is UiText) it.asString(context) else it }
        if (resolved.isEmpty()) context.getString(id) else context.getString(id, *resolved.toTypedArray())
    }
}

@Composable
fun UiText.asString(): String = asString(LocalContext.current)

/**
 * Localized counterpart of [ApiError.userMessage] (which stays English in `:core`). Messages written by the backend are
 * shown as they are, like [ApiError.userMessage] does.
 */
fun ApiError.localized(): UiText {
    fun server(text: String?, fallback: Int): UiText = if (text != null) UiText.Raw(text) else UiText.Res(fallback)
    return when (this) {
        is ApiError.Validation -> server(serverMessage, R.string.error_bad_request)
        is ApiError.Conflict -> server(serverMessage, R.string.error_bad_request)
        is ApiError.Forbidden -> if (isVipRequired) UiText.Res(R.string.error_vip_required) else server(serverMessage, R.string.error_forbidden)
        ApiError.Unauthorized -> UiText.Res(R.string.error_session_ended)
        ApiError.NotFound -> UiText.Res(R.string.error_not_found)
        ApiError.PayloadTooLarge -> UiText.Res(R.string.error_file_too_large)
        is ApiError.QuotaReached -> server(serverMessage, R.string.error_too_many_requests)
        is ApiError.Server -> UiText.Res(R.string.error_server)
        ApiError.BadGateway -> UiText.Res(R.string.error_bad_gateway)
        is ApiError.ServiceBusy -> UiText.Res(R.string.error_service_busy)
        is ApiError.UnexpectedStatus, ApiError.Decoding, ApiError.InvalidRequest -> UiText.Res(R.string.error_generic)
        ApiError.Network -> UiText.Res(R.string.error_offline)
    }
}
