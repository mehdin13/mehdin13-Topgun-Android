package app.topgun.android.features.details

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Load
import app.topgun.android.data.UserState
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGDetailSpace
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.DetailCircleButton
import app.topgun.android.designsystem.components.DetailStars
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ErrorState
import java.util.Locale

/**
 * iOS `CommentsListView`. Opened only after the detail has loaded, so the comments request never runs alongside the
 * detail request (the API allows about 4 requests per second).
 */
@Composable
internal fun CommentsPage(aircraftId: Int, averageRating: Double?, user: UserState, a: DetailActions, onClose: () -> Unit) {
    val vm = containerViewModel("comments-$aircraftId") { CommentsViewModel(it.api, aircraftId, it.prefs.userId) }
    val ui by vm.state.collectAsStateWithLifecycle()
    var text by rememberSaveable { mutableStateOf("") }
    var showSignIn by rememberSaveable { mutableStateOf(false) }
    val average = averageRating ?: 0.0
    val averageText = String.format(Locale.US, "%.1f", average)
    val averageDescription = stringResource(R.string.details_average_rating_cd, averageText)
    val sendDescription = stringResource(R.string.details_send_comment)
    val loaded = ui.comments as? Load.Loaded

    Column(Modifier.fillMaxSize().background(TG.colors.background).statusBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = TGSpace.s, vertical = TGSpace.s), verticalAlignment = Alignment.CenterVertically) {
            DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ds_back), onClose)
            Icon(Icons.Filled.Forum, contentDescription = null, tint = TG.colors.accent, modifier = Modifier.padding(start = TGSpace.m))
            Text(stringResource(R.string.details_comments), style = TGText.sailecBold(20), color = TG.colors.textPrimary, modifier = Modifier.padding(start = TGSpace.s))
            Text("(${loaded?.value?.size ?: 0})", style = TGText.sailecRegular(20), color = TG.colors.textSecondary, modifier = Modifier.padding(start = TGSpace.xs).weight(1f))
            DetailStars(Math.round(average).toInt(), TGDetailSize.smallStar)
            Text(
                averageText, style = TGText.sailecBold(14), color = TG.colors.textPrimary,
                modifier = Modifier.padding(start = TGSpace.xs).semantics { contentDescription = averageDescription },
            )
        }
        HorizontalDivider(color = TGBrand.gray.copy(alpha = 0.4f))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (val c = ui.comments) {
                Load.Loading -> CircularProgressIndicator(color = TG.colors.accent, modifier = Modifier.align(Alignment.Center))
                is Load.Failed -> ErrorState(c.error.userMessage, Modifier.align(Alignment.Center), onRetry = vm::load)
                is Load.Loaded ->
                    if (c.value.isEmpty()) {
                        EmptyState(stringResource(R.string.details_no_comments), Modifier.align(Alignment.Center), Icons.Filled.Forum)
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = TGSpace.l, vertical = TGSpace.m),
                            verticalArrangement = Arrangement.spacedBy(TGSpace.l),
                        ) { items(c.value, key = { it.id }) { CommentItem(it) } }
                    }
            }
        }

        ui.sendError?.let { Text(stringResource(R.string.details_comment_failed, it), style = TGText.sailecRegular(12), color = TGBrand.red, modifier = Modifier.padding(horizontal = TGSpace.l)) }
        HorizontalDivider(color = TGBrand.gray.copy(alpha = 0.4f))
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = TGSpace.l, vertical = TGSpace.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
        ) {
            Box(Modifier.size(TGDetailSize.inputAvatar).background(TGBrand.gray.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = TG.colors.textSecondary)
            }
            OutlinedTextField(
                value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.details_write_comment), style = TGText.sailecRegular(14)) },
                textStyle = TGText.sailecRegular(14), shape = TGDetailSize.inputRadius, maxLines = 4,
            )
            val canSend = text.isNotBlank() && !ui.isSending
            Box(
                Modifier.size(TGSize.tapMin).clickable(enabled = canSend, role = Role.Button) {
                    if (!user.isLoggedIn) showSignIn = true else vm.post(text) { ok -> if (ok) text = "" }
                }.semantics { contentDescription = sendDescription },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(TGDetailSize.sendButton).background(if (canSend) TG.colors.accent else TGBrand.gray, CircleShape), contentAlignment = Alignment.Center) {
                    if (ui.isSending) CircularProgressIndicator(color = TGDetailColors.onPhoto, strokeWidth = TGDetailSizeStroke, modifier = Modifier.size(TGSpace.l))
                    else Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = TGDetailColors.onPhoto)
                }
            }
        }
    }

    if (showSignIn) {
        AlertDialog(
            onDismissRequest = { showSignIn = false },
            title = { Text(stringResource(R.string.ds_sign_in_required), style = TGText.sailecBold(18)) },
            text = { Text(stringResource(R.string.details_sign_in_to_comment), style = TGText.sailecRegular(14)) },
            confirmButton = { TextButton({ showSignIn = false; a.onShowSignIn() }) { Text(stringResource(R.string.ds_sign_in)) } },
            dismissButton = { TextButton({ showSignIn = false }) { Text(stringResource(R.string.ds_ok)) } },
        )
    }
}

private val TGDetailSizeStroke = TGDetailSpace.hair

/** iOS `CommentRowView`: initial avatar, name, time, text. */
@Composable
private fun CommentItem(c: CommentRow) {
    val username = if (c.isSelf) stringResource(R.string.details_comment_you) else stringResource(R.string.details_comment_user, c.userId)
    val ago = c.timeAgo?.let {
        when (it.unit) {
            AgoUnit.JustNow -> stringResource(R.string.details_time_just_now)
            AgoUnit.Minutes -> stringResource(R.string.details_time_minutes_ago, it.amount)
            AgoUnit.Hours -> stringResource(R.string.details_time_hours_ago, it.amount)
            AgoUnit.Days -> stringResource(R.string.details_time_days_ago, it.amount)
        }
    }.orEmpty()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TGSpace.m)) {
        Box(Modifier.size(TGDetailSize.avatar).background(TGBrand.gray.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
            Text(username.take(1).uppercase(), style = TGText.sailecBold(16), color = TG.colors.textPrimary)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(username, style = TGText.sailecBold(14), color = TG.colors.textPrimary, modifier = Modifier.weight(1f))
                if (ago.isNotEmpty()) Text(ago, style = TGText.sailecRegular(11), color = TG.colors.textSecondary)
            }
            Text(c.text, style = TGText.sailecRegular(16), color = TG.colors.textPrimary)
            HorizontalDivider(color = TGBrand.gray.copy(alpha = 0.3f))
        }
    }
}
