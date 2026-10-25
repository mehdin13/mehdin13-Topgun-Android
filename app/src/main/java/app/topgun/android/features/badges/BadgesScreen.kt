package app.topgun.android.features.badges

import androidx.compose.ui.res.stringResource
import app.topgun.android.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Load
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountColors
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AccountTopBar
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.LoadingState
import app.topgun.android.designsystem.components.TGImage
import app.topgun.core.model.Badge

/** iOS `BadgeView`: 3-column grid of round badges; locked ones are dimmed with a lock and cannot be opened. */
@Composable
fun BadgesScreen(onBack: () -> Unit) {
    val vm = containerViewModel { BadgesViewModel(it.api, isSignedIn = { it.user.state.value.isLoggedIn }) }
    val state by vm.state.collectAsStateWithLifecycle()
    var openId by rememberSaveable { mutableStateOf<Int?>(null) }
    val badges = (state as? Load.Loaded)?.value.orEmpty()
    val open = badges.firstOrNull { it.id == openId && it.isHave }

    BackHandler(enabled = open != null) { openId = null }

    Column(Modifier.fillMaxSize().background(TG.colors.background)) {
        if (open != null) {
            AccountTopBar(open.name.orEmpty().ifBlank { stringResource(R.string.badges_fallback_name) }, onBack = { openId = null })
            BadgeDetail(open)
            return@Column
        }
        AccountTopBar(stringResource(R.string.badges_title), onBack = onBack)
        when (val s = state) {
            Load.Loading -> LoadingState(stringResource(R.string.badges_loading))
            is Load.Failed -> ErrorState(s.error.userMessage, onRetry = vm::load)
            is Load.Loaded -> if (s.value.isEmpty()) {
                EmptyState(stringResource(R.string.badges_sign_in))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(TGSpace.l),
                    horizontalArrangement = Arrangement.spacedBy(TGAccountSize.gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(TGAccountSize.gridSpacing),
                ) {
                    items(s.value, key = { it.id }) { badge -> BadgeCell(badge, onClick = { openId = badge.id }) }
                }
            }
        }
    }
}

@Composable
private fun BadgeCell(badge: Badge, onClick: () -> Unit) {
    val name = badge.name.orEmpty()
    val label = if (badge.isHave) name else stringResource(R.string.badges_locked, name)
    Column(
        Modifier.fillMaxWidth()
            .then(if (badge.isHave) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
            TGImage(
                badge.icon, contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape).alpha(if (badge.isHave) 1f else 0.5f),
            )
            if (!badge.isHave) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = TGAccountColors.white, modifier = Modifier.size(TGAccountSize.badgeLock))
            }
        }
        Text(
            name, style = TGText.sailecRegular(12), color = TG.colors.textPrimary, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(),
        )
    }
}
