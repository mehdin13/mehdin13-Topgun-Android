package com.mehdin13.dreamland.features.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mehdin13.dreamland.core.model.PublicStoryDto
import com.mehdin13.dreamland.core.network.FeedSort
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
import com.mehdin13.dreamland.designsystem.components.DLChip
import com.mehdin13.dreamland.designsystem.components.DLIconName
import com.mehdin13.dreamland.designsystem.components.EmptyStateView
import com.mehdin13.dreamland.designsystem.components.ErrorView
import com.mehdin13.dreamland.designsystem.components.FavoriteState
import com.mehdin13.dreamland.designsystem.components.LoadingView
import com.mehdin13.dreamland.designsystem.components.SegmentedControl
import com.mehdin13.dreamland.designsystem.components.StoryCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Category chips. The label doubles as the `category` filter until the backend's genre list is confirmed (BACKEND_GAPS #19). */
val FeedCategories = listOf("Adventure", "Bedtime", "Animals", "Space", "Fairy tale")

data class FeedState(
    val stories: Load<List<PublicStoryDto>> = Load.Loading,
    val sort: FeedSort = FeedSort.RECENT,
    val category: String? = null,
    val search: String = "",
    val updating: Set<Int> = emptySet(),
)

class FeedViewModel(private val api: StoryApi) : ViewModel() {
    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    fun setSort(sort: FeedSort) { _state.update { it.copy(sort = sort) }; load() }
    fun setCategory(category: String?) { _state.update { it.copy(category = if (it.category == category) null else category) }; load() }
    fun setSearch(text: String) { _state.update { it.copy(search = text) }; load() }

    fun load() {
        loadJob?.cancel()
        _state.update { it.copy(stories = Load.Loading) }
        val s = _state.value
        loadJob = viewModelScope.launch {
            val result = runCatching { api.publicStories(s.sort, s.category, s.search) }
            _state.update { it.copy(stories = result.fold({ list -> Load.Loaded(list) }, { e -> Load.Failed(e.asApiError()) })) }
        }
    }

    fun toggleFavorite(story: PublicStoryDto) {
        if (story.id in _state.value.updating) return
        _state.update { it.copy(updating = it.updating + story.id) }
        viewModelScope.launch {
            api.toggleFavorite(story) { next ->
                _state.update { s -> s.copy(stories = (s.stories as? Load.Loaded)?.let { Load.Loaded(it.value.replacing(next)) } ?: s.stories) }
            }
            _state.update { it.copy(updating = it.updating - story.id) }
        }
    }
}

/** Discover: public completed stories. The child's original paintings never appear here (the DTO has no such field). */
@Composable
fun FeedScreen(onOpenStory: (id: Int, isOwner: Boolean) -> Unit, contentPadding: PaddingValues) {
    val vm = containerViewModel { FeedViewModel(it.stories) }
    val media = rememberContainer().media
    val state by vm.state.collectAsStateWithLifecycle()
    val margin = DLLayout.screenMarginCompact
    var query by remember { mutableStateOf("") }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(DLLayout.gridTileMinCompact),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = margin, end = margin,
            top = contentPadding.calculateTopPadding() + DLSpacing.space8,
            bottom = contentPadding.calculateBottomPadding() + DLSpacing.space32,
        ),
        horizontalArrangement = Arrangement.spacedBy(DLLayout.gridGutterCompact),
        verticalArrangement = Arrangement.spacedBy(DLLayout.gridGutterCompact),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(DLSpacing.space12)) {
                Text("Discover", style = DL.type.displayMedium, color = DL.colors.textPrimary, modifier = Modifier.semantics { heading() })
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search stories") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { vm.setSearch(query) }),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(DLSpacing.space8)) {
                    items(FeedCategories) { label ->
                        DLChip(label, selected = state.category == label, onClick = { vm.setCategory(label) })
                    }
                }
                SegmentedControl(
                    options = FeedSort.entries.map { it to it.label },
                    selected = state.sort,
                    onSelect = vm::setSort,
                )
            }
        }
        when (val load = state.stories) {
            Load.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { LoadingView(Modifier.padding(DLSpacing.space48)) }
            is Load.Failed -> item(span = { GridItemSpan(maxLineSpan) }) { ErrorView(load.error.userMessage, onRetry = vm::load) }
            is Load.Loaded -> if (load.value.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateView(DLIconName.Search, "No stories found", "Try another word or category.")
                }
            } else {
                items(load.value, key = { it.id }) { story ->
                    StoryCard(
                        title = story.title,
                        coverUrl = media.resolve(story.coverImageUrl),
                        seed = story.id,
                        meta = story.theme?.replaceFirstChar { it.uppercase() },
                        favorite = FavoriteState(story.isFavoritedByMe, story.favoriteCount, isUpdating = story.id in state.updating),
                        onToggleFavorite = { vm.toggleFavorite(story) },
                        onOpen = { onOpenStory(story.id, false) },
                    )
                }
            }
        }
    }
}
