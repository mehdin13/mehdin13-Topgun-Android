package com.mehdin13.dreamland.features.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mehdin13.dreamland.core.model.PublicStoryDto
import com.mehdin13.dreamland.core.model.StorySummaryDto
import com.mehdin13.dreamland.core.network.ApiError
import com.mehdin13.dreamland.core.network.StoryApi
import com.mehdin13.dreamland.data.Load
import com.mehdin13.dreamland.data.asApiError
import com.mehdin13.dreamland.data.containerViewModel
import com.mehdin13.dreamland.data.rememberContainer
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLLayout
import com.mehdin13.dreamland.designsystem.DLSpacing
import com.mehdin13.dreamland.designsystem.components.DLIconName
import com.mehdin13.dreamland.designsystem.components.EmptyStateView
import com.mehdin13.dreamland.designsystem.components.ErrorView
import com.mehdin13.dreamland.designsystem.components.LoadingView
import com.mehdin13.dreamland.designsystem.components.SegmentedControl
import com.mehdin13.dreamland.designsystem.components.StoryCard
import com.mehdin13.dreamland.designsystem.components.StoryCardLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LibraryTab(val label: String) { MyStories("My stories"), Saved("Saved"), Favorites("Favorites") }

data class LibraryState(
    val mine: Load<List<StorySummaryDto>> = Load.Loading,
    val saved: Load<List<PublicStoryDto>> = Load.Loading,
    val favorites: Load<List<PublicStoryDto>> = Load.Loading,
)

class LibraryViewModel(private val api: StoryApi) : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = LibraryState()
        viewModelScope.launch {
            val mine = runCatching { api.myStories() }
            _state.update { it.copy(mine = mine.fold({ v -> Load.Loaded(v) }, { e -> Load.Failed(e.asApiError()) })) }
        }
        viewModelScope.launch {
            val saved = runCatching { api.myCollection() }
            _state.update { it.copy(saved = saved.fold({ v -> Load.Loaded(v) }, { e -> Load.Failed(e.asApiError()) })) }
        }
        viewModelScope.launch {
            val favorites = runCatching { api.myFavorites() }
            // my_favorites is not on the backend yet (BACKEND_GAPS #43): a 404 reads as an empty list, not an error.
            _state.update {
                it.copy(favorites = favorites.fold({ v -> Load.Loaded(v) }, { e ->
                    val err = e.asApiError()
                    if (err == ApiError.NotFound) Load.Loaded(emptyList()) else Load.Failed(err)
                }))
            }
        }
    }
}

@Composable
fun LibraryScreen(onOpenStory: (id: Int, isOwner: Boolean) -> Unit, contentPadding: PaddingValues) {
    val vm = containerViewModel { LibraryViewModel(it.stories) }
    val media = rememberContainer().media
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(LibraryTab.MyStories) }
    val margin = DLLayout.screenMarginCompact

    LazyColumn(
        contentPadding = PaddingValues(
            start = margin, end = margin,
            top = contentPadding.calculateTopPadding() + DLSpacing.space8,
            bottom = contentPadding.calculateBottomPadding() + DLSpacing.space32,
        ),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space12),
    ) {
        item {
            Text("Library", style = DL.type.displayMedium, color = DL.colors.textPrimary, modifier = Modifier.semantics { heading() })
        }
        item { SegmentedControl(LibraryTab.entries.map { it to it.label }, tab, { tab = it }) }

        when (tab) {
            LibraryTab.MyStories -> section(state.mine, vm::refresh, empty = Triple(DLIconName.Book, "No stories yet", "Stories you make will be kept here.")) { list ->
                items(list, key = { it.id }) { s ->
                    StoryCard(
                        title = s.title.ifBlank { "Untitled story" },
                        coverUrl = null,
                        seed = s.id,
                        meta = s.theme?.replaceFirstChar { it.uppercase() },
                        layout = StoryCardLayout.Row,
                        onOpen = { onOpenStory(s.id, true) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            LibraryTab.Saved -> publicSection(state.saved, vm::refresh, media, onOpenStory, Triple(DLIconName.Bookmark, "Nothing saved yet", "Save a story from Discover to read it later."))
            LibraryTab.Favorites -> publicSection(state.favorites, vm::refresh, media, onOpenStory, Triple(DLIconName.Heart, "No favorites yet", "Tap the heart on a story you love."))
        }
    }
}

private fun <T> androidx.compose.foundation.lazy.LazyListScope.section(
    load: Load<List<T>>,
    onRetry: () -> Unit,
    empty: Triple<DLIconName, String, String>,
    rows: androidx.compose.foundation.lazy.LazyListScope.(List<T>) -> Unit,
) {
    when (load) {
        Load.Loading -> item { LoadingView(Modifier.padding(DLSpacing.space48)) }
        is Load.Failed -> item { ErrorView(load.error.userMessage, onRetry = onRetry) }
        is Load.Loaded -> if (load.value.isEmpty()) {
            item { EmptyStateView(empty.first, empty.second, empty.third) }
        } else {
            rows(load.value)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.publicSection(
    load: Load<List<PublicStoryDto>>,
    onRetry: () -> Unit,
    media: com.mehdin13.dreamland.core.network.MediaUrlResolver,
    onOpenStory: (Int, Boolean) -> Unit,
    empty: Triple<DLIconName, String, String>,
) = section(load, onRetry, empty) { list ->
    items(list, key = { it.id }) { s ->
        StoryCard(
            title = s.title,
            coverUrl = media.resolve(s.coverImageUrl),
            seed = s.id,
            meta = s.theme?.replaceFirstChar { it.uppercase() },
            layout = StoryCardLayout.Row,
            onOpen = { onOpenStory(s.id, false) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
