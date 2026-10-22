package com.mehdin13.dreamland.features.story

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.mehdin13.dreamland.core.model.StoryDto
import com.mehdin13.dreamland.core.model.StoryStatus
import com.mehdin13.dreamland.core.network.StoryApi
import com.mehdin13.dreamland.data.Load
import com.mehdin13.dreamland.data.asApiError
import com.mehdin13.dreamland.data.containerViewModel
import com.mehdin13.dreamland.data.rememberContainer
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLLayout
import com.mehdin13.dreamland.designsystem.DLRadius
import com.mehdin13.dreamland.designsystem.DLSpacing
import com.mehdin13.dreamland.designsystem.components.DLButton
import com.mehdin13.dreamland.designsystem.components.DLButtonSize
import com.mehdin13.dreamland.designsystem.components.DLButtonVariant
import com.mehdin13.dreamland.designsystem.components.DLIconButton
import com.mehdin13.dreamland.designsystem.components.DLIconName
import com.mehdin13.dreamland.designsystem.components.ErrorView
import com.mehdin13.dreamland.designsystem.components.FavoriteButton
import com.mehdin13.dreamland.designsystem.components.LoadingView
import com.mehdin13.dreamland.designsystem.components.RateStars
import com.mehdin13.dreamland.designsystem.components.StoryCover
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class StoryState(
    val story: Load<StoryDto> = Load.Loading,
    val isBusy: Boolean = false,
    val message: String? = null,
)

class StoryViewModel(private val api: StoryApi, private val id: Int) : ViewModel() {
    private val _state = MutableStateFlow(StoryState())
    val state: StateFlow<StoryState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = StoryState()
        viewModelScope.launch {
            val result = runCatching { api.story(id) }
            _state.update { it.copy(story = result.fold({ s -> Load.Loaded(s) }, { e -> Load.Failed(e.asApiError()) })) }
        }
    }

    private fun current(): StoryDto? = (_state.value.story as? Load.Loaded)?.value

    private fun set(story: StoryDto, busy: Boolean = false, message: String? = null) =
        _state.update { StoryState(Load.Loaded(story), busy, message) }

    /** Optimistic favorite with rollback; the count comes from the server's reply. */
    fun toggleFavorite() {
        val story = current() ?: return
        if (_state.value.isBusy) return
        val optimistic = story.copy(
            isFavoritedByMe = !story.isFavoritedByMe,
            favoriteCount = (story.favoriteCount + if (story.isFavoritedByMe) -1 else 1).coerceAtLeast(0),
        )
        set(optimistic, busy = true)
        viewModelScope.launch {
            try {
                val reply = if (story.isFavoritedByMe) api.unfavorite(id) else api.favorite(id)
                set(optimistic.copy(isFavoritedByMe = reply.favorited, favoriteCount = reply.favoriteCount))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // Favorite is public-only: a 404 means this story is not available to favorite.
                set(story, message = e.asApiError().userMessage)
            }
        }
    }

    fun toggleSaved() {
        val story = current() ?: return
        if (_state.value.isBusy) return
        set(story.copy(isSavedByMe = !story.isSavedByMe), busy = true)
        viewModelScope.launch {
            try {
                if (story.isSavedByMe) api.unsave(id) else api.save(id)
                set(story.copy(isSavedByMe = !story.isSavedByMe))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                set(story, message = e.asApiError().userMessage)
            }
        }
    }

    fun rate(score: Int) {
        val story = current() ?: return
        viewModelScope.launch {
            try {
                api.rate(id, score)
                load()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                set(story, message = e.asApiError().userMessage)
            }
        }
    }

    /** Owner only: makes the story public (shareable) or private again. */
    fun toggleShared() {
        val story = current() ?: return
        set(story, busy = true)
        viewModelScope.launch {
            try {
                set(if (story.isPublic) api.unshare(id) else api.share(id))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                set(story, message = e.asApiError().userMessage)
            }
        }
    }
}

@Composable
fun StoryScreen(id: Int, isOwner: Boolean, onBack: () -> Unit) {
    val vm = containerViewModel(key = "story-$id") { StoryViewModel(it.stories, id) }
    val media = rememberContainer().media
    val state by vm.state.collectAsStateWithLifecycle()
    val c = DL.colors

    Scaffold(
        containerColor = c.backgroundPrimary,
        topBar = {
            Row(Modifier.padding(horizontal = DLSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
                DLIconButton(DLIconName.ChevronLeft, label = "Back", onClick = onBack)
            }
        },
    ) { padding ->
        when (val load = state.story) {
            Load.Loading -> LoadingView(Modifier.padding(padding))
            is Load.Failed -> Column(Modifier.padding(padding).padding(DLLayout.screenMarginCompact)) {
                ErrorView(load.error.userMessage, onRetry = vm::load)
            }
            is Load.Loaded -> StoryContent(load.value, isOwner, state, vm, media::resolve, Modifier.padding(padding))
        }
    }
}

@Composable
private fun StoryContent(
    story: StoryDto,
    isOwner: Boolean,
    state: StoryState,
    vm: StoryViewModel,
    resolve: (String?) -> String?,
    modifier: Modifier,
) {
    val c = DL.colors
    val context = LocalContext.current
    fun open(url: String?) {
        val uri = url?.let(Uri::parse) ?: return
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DLLayout.screenMarginCompact)
            .padding(bottom = DLSpacing.space32),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StoryCover(
            imageUrl = resolve(story.coverImageUrl),
            title = story.title,
            seed = story.id,
            corner = DLRadius.xl,
            modifier = Modifier.widthIn(max = DLLayout.readableWidth),
        )
        Text(
            story.title.ifBlank { "Untitled story" },
            style = DL.type.displayMedium,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            listOfNotNull(story.theme, story.genre, story.ageRange?.let { "Ages $it" }).joinToString(" · ").replaceFirstChar { it.uppercase() },
            style = DL.type.metadataLabel,
            color = c.textSecondary,
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (story.isPublic) {
                FavoriteButton(story.isFavoritedByMe, story.favoriteCount, onToggle = vm::toggleFavorite, enabled = !state.isBusy)
            }
            DLButton(
                if (story.isSavedByMe) "Saved" else "Save",
                onClick = vm::toggleSaved,
                variant = DLButtonVariant.Tertiary,
                size = DLButtonSize.Medium,
                icon = DLIconName.Bookmark,
                enabled = story.isPublic && !state.isBusy, // save is public-only (BACKEND_GAPS #48)
            )
            if (isOwner) {
                DLButton(
                    if (story.isPublic) "Make private" else "Share",
                    onClick = vm::toggleShared,
                    variant = DLButtonVariant.Tertiary,
                    size = DLButtonSize.Medium,
                    icon = DLIconName.Share,
                    enabled = story.status == StoryStatus.COMPLETED && !state.isBusy,
                )
            }
        }

        state.message?.let { ErrorView(it) }

        when (story.status) {
            StoryStatus.GENERATING -> ErrorView("This story is still being made. Check back in a moment.", onRetry = vm::load)
            StoryStatus.FAILED -> ErrorView("This story couldn't be finished.")
            else -> Unit
        }

        // Each output shows its own pending/failed state instead of being silently hidden.
        // TODO(session 2): in-app audio player (Media3) and PDF viewer; for now both open in the system app.
        if (story.generateAudio) {
            DLButton(
                if (story.audioUrl != null) "Listen" else "Narration is being made",
                onClick = { open(resolve(story.audioUrl)) },
                enabled = story.audioUrl != null,
                icon = DLIconName.Speaker,
                variant = DLButtonVariant.Secondary,
                fullWidth = true,
            )
        }
        if (story.generatePdf) {
            DLButton(
                if (story.pdfUrl != null) "Open the book (PDF)" else "The book is being made",
                onClick = { open(resolve(story.pdfUrl)) },
                enabled = story.pdfUrl != null,
                icon = DLIconName.Doc,
                variant = DLButtonVariant.Secondary,
                fullWidth = true,
            )
        }

        Column(Modifier.widthIn(max = DLLayout.readableWidth), verticalArrangement = Arrangement.spacedBy(DLSpacing.space16)) {
            story.content.split(Regex("\n\\s*\n")).filter { it.isNotBlank() }.forEach { paragraph ->
                Text(paragraph.trim(), style = DL.type.storyBody, color = c.textStory)
            }
        }

        if (story.status == StoryStatus.COMPLETED && story.isPublic) {
            Text("How did you like it?", style = DL.type.headline, color = c.textPrimary)
            RateStars(score = 0, onRate = vm::rate) // the viewer's own rating isn't on the story (BACKEND_GAPS #17)
            if (story.ratingCount > 0) {
                Text("%.1f from %d ratings".format(story.averageRating, story.ratingCount), style = DL.type.caption, color = c.textSecondary)
            }
        }

        // The child's original painting is private: owner only, never in Discover or shared views.
        val painting = if (isOwner) resolve(story.sourcePaintingUrl) else null
        if (painting != null) {
            Text("Your painting", style = DL.type.headline, color = c.textPrimary, modifier = Modifier.fillMaxWidth())
            AsyncImage(
                model = painting,
                contentDescription = "The painting this story came from",
                contentScale = ContentScale.Fit,
                modifier = Modifier.widthIn(max = DLLayout.readableWidth).fillMaxWidth(),
            )
        }
    }
}
