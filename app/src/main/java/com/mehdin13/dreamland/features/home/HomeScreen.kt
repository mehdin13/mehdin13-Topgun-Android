package com.mehdin13.dreamland.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mehdin13.dreamland.core.model.GenerationQuotaDto
import com.mehdin13.dreamland.core.model.PublicStoryDto
import com.mehdin13.dreamland.core.model.StorySummaryDto
import com.mehdin13.dreamland.core.network.MediaUrlResolver
import com.mehdin13.dreamland.core.network.StoryApi
import com.mehdin13.dreamland.data.Load
import com.mehdin13.dreamland.data.asApiError
import com.mehdin13.dreamland.data.containerViewModel
import com.mehdin13.dreamland.data.rememberContainer
import com.mehdin13.dreamland.data.replacing
import com.mehdin13.dreamland.data.toggleFavorite
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLLayout
import com.mehdin13.dreamland.designsystem.DLSpacing
import com.mehdin13.dreamland.designsystem.components.DLButton
import com.mehdin13.dreamland.designsystem.components.DLButtonSize
import com.mehdin13.dreamland.designsystem.components.DLButtonVariant
import com.mehdin13.dreamland.designsystem.components.DLIconName
import com.mehdin13.dreamland.designsystem.components.EmptyStateView
import com.mehdin13.dreamland.designsystem.components.ErrorView
import com.mehdin13.dreamland.designsystem.components.FavoriteState
import com.mehdin13.dreamland.designsystem.components.QuotaCard
import com.mehdin13.dreamland.designsystem.components.SectionHeader
import com.mehdin13.dreamland.designsystem.components.StoryCard
import com.mehdin13.dreamland.designsystem.components.StoryCardLayout
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Home content. Each section loads on its own, so one failure never blanks the screen. */
data class HomeState(
    val mine: Load<List<StorySummaryDto>> = Load.Loading,
    val mostFavorited: Load<List<PublicStoryDto>> = Load.Loading,
    val recent: Load<List<PublicStoryDto>> = Load.Loading,
    val quota: GenerationQuotaDto.ImageStoryQuota? = null,
    val updating: Set<Int> = emptySet(),
)

class HomeViewModel(private val api: StoryApi) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.update { HomeState() }
        viewModelScope.launch {
            coroutineScope {
                val mine = async { runCatching { api.myStories() } }
                val favorites = async { runCatching { api.mostFavorited() } }
                val recent = async { runCatching { api.publicStories() } }
                val quota = async { runCatching { api.quota() } }
                _state.update { it.copy(mine = mine.await().toLoad()) }
                _state.update { it.copy(mostFavorited = favorites.await().toLoad()) }
                _state.update { it.copy(recent = recent.await().toLoad()) }
                _state.update { it.copy(quota = quota.await().getOrNull()?.imageStory) }
            }
        }
    }

    fun toggleFavorite(story: PublicStoryDto) {
        if (story.id in _state.value.updating) return
        _state.update { it.copy(updating = it.updating + story.id) }
        viewModelScope.launch {
            api.toggleFavorite(story) { next ->
                _state.update { s ->
                    s.copy(
                        mostFavorited = s.mostFavorited.mapLoaded { it.replacing(next) },
                        recent = s.recent.mapLoaded { it.replacing(next) },
                    )
                }
            }
            _state.update { it.copy(updating = it.updating - story.id) }
        }
    }
}

private fun <T> Result<T>.toLoad(): Load<T> = fold({ Load.Loaded(it) }, { Load.Failed(it.asApiError()) })

private fun <T> Load<List<T>>.mapLoaded(transform: (List<T>) -> List<T>): Load<List<T>> =
    if (this is Load.Loaded) Load.Loaded(transform(value)) else this

@Composable
fun HomeScreen(
    onOpenStory: (id: Int, isOwner: Boolean) -> Unit,
    onCreate: () -> Unit,
    onCreateFromPainting: () -> Unit,
    onSeeAllDiscover: () -> Unit,
    contentPadding: PaddingValues,
) {
    val vm = containerViewModel { HomeViewModel(it.stories) }
    val media = rememberContainer().media
    val state by vm.state.collectAsStateWithLifecycle()
    val margin = DLLayout.screenMarginCompact
    val c = DL.colors

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + DLSpacing.space8,
            bottom = contentPadding.calculateBottomPadding() + DLSpacing.space32,
        ),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space32),
    ) {
        item {
            Column(Modifier.padding(horizontal = margin), verticalArrangement = Arrangement.spacedBy(DLSpacing.space8)) {
                Text("What shall we dream up?", style = DL.type.displayMedium, color = c.textPrimary, modifier = Modifier.semantics { heading() })
                Text("Make a new story, or pick up where you left off.", style = DL.type.bodyRegular, color = c.textSecondary)
            }
        }
        item {
            Column(Modifier.padding(horizontal = margin), verticalArrangement = Arrangement.spacedBy(DLSpacing.space12)) {
                Row(horizontalArrangement = Arrangement.spacedBy(DLSpacing.space12)) {
                    DLButton("Create story", onClick = onCreate, icon = DLIconName.Sparkles, modifier = Modifier.weight(1f))
                    DLButton("From painting", onClick = onCreateFromPainting, icon = DLIconName.Brush, variant = DLButtonVariant.Secondary, modifier = Modifier.weight(1f))
                }
                state.quota?.let { QuotaCard(remaining = it.remainingToday, total = it.dailyLimit) }
            }
        }
        item {
            Section("Your stories", margin) {
                when (val mine = state.mine) {
                    Load.Loading -> Text("Loading…", style = DL.type.bodySmall, color = c.textSecondary, modifier = Modifier.padding(horizontal = margin))
                    is Load.Failed -> ErrorView(mine.error.userMessage, Modifier.padding(horizontal = margin), onRetry = vm::refresh)
                    is Load.Loaded -> if (mine.value.isEmpty()) {
                        EmptyStateView(DLIconName.Book, "No stories yet", "Your family's stories will show up here.", actionLabel = "Create a story", onAction = onCreate)
                    } else {
                        Column(Modifier.padding(horizontal = margin), verticalArrangement = Arrangement.spacedBy(DLSpacing.space12)) {
                            mine.value.take(5).forEach { s ->
                                StoryCard(
                                    title = s.title.ifBlank { "Untitled story" },
                                    coverUrl = null, // StoryListSerializer has no cover (BACKEND_GAPS #50)
                                    seed = s.id,
                                    meta = s.theme?.replaceFirstChar { it.uppercase() },
                                    layout = StoryCardLayout.Row,
                                    onOpen = { onOpenStory(s.id, true) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Section("Most favorited", margin, actionLabel = "See all", onAction = onSeeAllDiscover) {
                PublicRow(state.mostFavorited, state.updating, media, margin, onRetry = vm::refresh, onOpen = { onOpenStory(it, false) }, onFavorite = vm::toggleFavorite)
            }
        }
        item {
            Section("Recent from other families", margin, actionLabel = "See all", onAction = onSeeAllDiscover) {
                PublicRow(state.recent, state.updating, media, margin, onRetry = vm::refresh, onOpen = { onOpenStory(it, false) }, onFavorite = vm::toggleFavorite)
            }
        }
    }
}

@Composable
private fun Section(title: String, margin: androidx.compose.ui.unit.Dp, actionLabel: String? = null, onAction: () -> Unit = {}, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DLSpacing.space12)) {
        SectionHeader(title, Modifier.padding(horizontal = margin), actionLabel = actionLabel, onAction = onAction)
        content()
    }
}

@Composable
private fun PublicRow(
    load: Load<List<PublicStoryDto>>,
    updating: Set<Int>,
    media: MediaUrlResolver,
    margin: androidx.compose.ui.unit.Dp,
    onRetry: () -> Unit,
    onOpen: (Int) -> Unit,
    onFavorite: (PublicStoryDto) -> Unit,
) {
    when (load) {
        Load.Loading -> Text("Loading…", style = DL.type.bodySmall, color = DL.colors.textSecondary, modifier = Modifier.padding(horizontal = margin))
        is Load.Failed -> ErrorView(load.error.userMessage, Modifier.padding(horizontal = margin), onRetry = onRetry)
        is Load.Loaded -> if (load.value.isEmpty()) {
            Text("Stories shared by other families will appear here.", style = DL.type.bodySmall, color = DL.colors.textSecondary, modifier = Modifier.padding(horizontal = margin))
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = margin), horizontalArrangement = Arrangement.spacedBy(DLSpacing.space12)) {
                items(load.value, key = { it.id }) { story ->
                    StoryCard(
                        title = story.title,
                        coverUrl = media.resolve(story.coverImageUrl),
                        seed = story.id,
                        meta = story.theme?.replaceFirstChar { it.uppercase() },
                        favorite = FavoriteState(story.isFavoritedByMe, story.favoriteCount, isUpdating = story.id in updating),
                        onToggleFavorite = { onFavorite(story) },
                        onOpen = { onOpen(story.id) },
                        modifier = Modifier.width(168.dp),
                    )
                }
            }
        }
    }
}
