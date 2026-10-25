// ANDROID-ONLY
package app.topgun.android.platform.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.topgun.android.R
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.TGImage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/** Media3 ExoPlayer for gallery videos and android-youtube-player for YouTube links. */
internal object VideoUi {
    @Composable
    fun Player(url: String, modifier: Modifier) {
        val context = LocalContext.current
        val player = remember(url) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }
        }
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        DisposableEffect(player, lifecycle) {
            val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) player.pause() }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
                player.release()
            }
        }
        AndroidView(
            factory = { PlayerView(it).apply { this.player = player; useController = true } },
            update = { it.player = player },
            modifier = modifier,
        )
    }

    /**
     * YouTube link, played by android-youtube-player 12.1.0 (IFrame player in a WebView, with YouTube's own controls and
     * fullscreen button). A thumbnail with a play button comes first; the player is created only after the tap (faster
     * page, one WebView less per details screen). When YouTube refuses to play the video (not embeddable, removed, ...)
     * the player is replaced by a message and a "Watch on YouTube" button that opens the YouTube app or the browser.
     */
    @Composable
    fun YouTube(url: String, modifier: Modifier) {
        val id = remember(url) { youTubeVideoId(url) }
        if (id == null) {
            Placeholder.Empty(modifier)
            return
        }
        val context = LocalContext.current
        var playing by rememberSaveable(id) { mutableStateOf(false) }
        var error by remember(id) { mutableStateOf<PlayerConstants.PlayerError?>(null) }
        var exitFullscreen by remember(id) { mutableStateOf<(() -> Unit)?>(null) }
        BackHandler(enabled = exitFullscreen != null) { exitFullscreen?.invoke() }

        Box(modifier.background(TGDetailColors.photoBlack), contentAlignment = Alignment.Center) {
            val failure = error
            when {
                failure != null -> YouTubeFailure(failure, onWatch = { openOnYouTube(context, id) })
                !playing -> {
                    TGImage(youTubeThumbnailUrl(id), contentDescription = null, modifier = Modifier.fillMaxSize())
                    Box(
                        Modifier.fillMaxSize().clickable(role = Role.Button, onClickLabel = "Play video") { playing = true }
                            .semantics { contentDescription = "Play video" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(TGDetailSize.playButton).background(TGDetailColors.photoBlack.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = TGDetailColors.onPhoto, modifier = Modifier.size(TGDetailSize.playButton / 2))
                        }
                    }
                }
                else -> YouTubePlayerHost(
                    videoId = id,
                    modifier = Modifier.fillMaxSize(),
                    onError = { error = it },
                    onFullscreenChanged = { exitFullscreen = it },
                )
            }
        }
    }

    /** The library's [YouTubePlayerView], initialised manually (see res/layout/view_youtube_player.xml) and tied to the lifecycle. */
    @Composable
    private fun YouTubePlayerHost(
        videoId: String,
        modifier: Modifier,
        onError: (PlayerConstants.PlayerError) -> Unit,
        onFullscreenChanged: ((() -> Unit)?) -> Unit,
    ) {
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val latestOnError by rememberUpdatedState(onError)
        val latestOnFullscreenChanged by rememberUpdatedState(onFullscreenChanged)
        val fullscreen = remember { YouTubeFullscreen(context) { latestOnFullscreenChanged(it) } }
        val playerView = remember(videoId) {
            (LayoutInflater.from(context).inflate(R.layout.view_youtube_player, null) as YouTubePlayerView).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                addFullscreenListener(fullscreen)
                val listener = object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadOrCueVideo(lifecycle, videoId, 0f)
                    }

                    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                        latestOnError(error)
                    }
                }
                // controls(1): YouTube's own control bar; fullscreen(1): its fullscreen button (handled by [YouTubeFullscreen]).
                val options = IFramePlayerOptions.Builder().controls(1).fullscreen(1).rel(0).build()
                initialize(listener, options)
            }
        }
        DisposableEffect(playerView) {
            lifecycle.addObserver(playerView) // pauses on ON_STOP, releases on ON_DESTROY
            onDispose {
                lifecycle.removeObserver(playerView)
                fullscreen.exit()
                // After ON_DESTROY the view has already released itself; releasing a WebView twice is not allowed.
                if (lifecycle.currentState != Lifecycle.State.DESTROYED) playerView.release()
            }
        }
        AndroidView(factory = { playerView }, modifier = modifier)
    }

    @Composable
    private fun YouTubeFailure(error: PlayerConstants.PlayerError, onWatch: () -> Unit) {
        val message = when (error) {
            PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> "The owner of this video does not allow it to be played in other apps."
            PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> "This video is no longer available."
            else -> "The video could not be played here."
        }
        Column(
            Modifier.fillMaxSize().padding(TGSpace.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TGSpace.m, Alignment.CenterVertically),
        ) {
            Text(message, style = TGText.sailecMedium(14), color = TGDetailColors.onPhoto, textAlign = TextAlign.Center)
            Button(onClick = onWatch, modifier = Modifier.defaultMinSize(minHeight = TGSize.tapMin), shape = TGDetailSize.buttonRadius) {
                Text("Watch on YouTube", style = TGText.sailecMedium(16))
            }
        }
    }

    /** The YouTube app when installed (vnd.youtube:), otherwise the watch page in the browser. */
    private fun openOnYouTube(context: Context, id: String) {
        val app = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(app)
        } catch (e: ActivityNotFoundException) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(youTubeWatchUrl(id))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }
}

/**
 * Shows the player's fullscreen view above the whole window (the library only hands over the View) and hides the
 * system bars while it is shown. [onChanged] gets the "exit fullscreen" action while fullscreen, null otherwise, so the
 * back button can leave fullscreen.
 */
private class YouTubeFullscreen(
    private val context: Context,
    private val onChanged: ((() -> Unit)?) -> Unit,
) : FullscreenListener {
    private var view: View? = null
    private var requestExit: (() -> Unit)? = null

    override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
        val activity = context.findActivity()
        if (activity == null) {
            exitFullscreen()
            return
        }
        (fullscreenView.parent as? ViewGroup)?.removeView(fullscreenView)
        fullscreenView.setBackgroundColor(android.graphics.Color.BLACK)
        val decor = activity.window.decorView as ViewGroup
        decor.addView(fullscreenView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        view = fullscreenView
        requestExit = exitFullscreen
        WindowInsetsControllerCompat(activity.window, decor).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        onChanged(exitFullscreen)
    }

    override fun onExitFullscreen() {
        val shown = view ?: return
        (shown.parent as? ViewGroup)?.removeView(shown)
        view = null
        requestExit = null
        context.findActivity()?.let { activity ->
            WindowInsetsControllerCompat(activity.window, activity.window.decorView).show(WindowInsetsCompat.Type.systemBars())
        }
        onChanged(null)
    }

    /** Leaves fullscreen when the player goes away while it is shown. */
    fun exit() {
        if (view != null) requestExit?.invoke()
        onExitFullscreen()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
