// ANDROID-ONLY
package app.topgun.android.platform.ui

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.TGImage
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/** Media3 ExoPlayer for gallery videos and a WebView embed for YouTube links. */
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
     * YouTube link: a thumbnail with a play button first; the WebView embed is created only after the tap (faster page,
     * no autoplay problems). The embed is hosted from an https page that is NOT youtube.com, with a proper origin and
     * `referrerpolicy`, because YouTube answers "Video player configuration error" (153) when the embedding page has
     * no valid origin/Referer. A "Watch on YouTube" fallback lives next to this player in the details screen.
     */
    @Composable
    fun YouTube(url: String, modifier: Modifier) {
        val id = remember(url) { youTubeVideoId(url) }
        if (id == null) {
            Placeholder.Empty(modifier)
            return
        }
        var playing by rememberSaveable(id) { mutableStateOf(false) }
        var failed by remember(id) { mutableStateOf(false) }
        Box(modifier.background(TGDetailColors.photoBlack), contentAlignment = Alignment.Center) {
            if (!playing || failed) {
                TGImage(youTubeThumbnailUrl(id), contentDescription = null, modifier = Modifier.fillMaxSize())
                Box(
                    Modifier.fillMaxSize().clickable(role = Role.Button, onClickLabel = "Play video") { failed = false; playing = true }
                        .semantics { contentDescription = "Play video" },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(TGDetailSize.playButton).background(TGDetailColors.photoBlack.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = TGDetailColors.onPhoto, modifier = Modifier.size(TGDetailSize.playButton / 2))
                    }
                }
                if (failed) {
                    Text(
                        "The video could not be loaded. Use Watch on YouTube below.",
                        style = TGText.sailecMedium(13), color = TGDetailColors.onPhoto,
                        modifier = Modifier.align(Alignment.BottomCenter).background(TGDetailColors.photoBlack.copy(alpha = 0.6f)).padding(TGSpace.s),
                    )
                }
            } else {
                YouTubeWebView(id, Modifier.fillMaxSize()) { failed = true }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun YouTubeWebView(id: String, modifier: Modifier, onError: () -> Unit) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(android.graphics.Color.BLACK)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadWithOverviewMode = true
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                            if (request.isForMainFrame) onError()
                        }

                        // Keep the player inside the embed; links out of it (logo, "Watch on YouTube") are not followed.
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = request.isForMainFrame && request.url.toString() != EMBED_ORIGIN + "/"
                    }
                    val embed = "https://www.youtube.com/embed/$id?playsinline=1&rel=0&autoplay=1&modestbranding=1&origin=$EMBED_ORIGIN"
                    val html = """<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
                        <meta name="referrer" content="strict-origin-when-cross-origin">
                        <style>html,body{margin:0;height:100%;background:#000}iframe{position:absolute;inset:0;width:100%;height:100%;border:0}</style></head>
                        <body><iframe src="$embed" title="YouTube video player" frameborder="0"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                        referrerpolicy="strict-origin-when-cross-origin"></iframe></body></html>"""
                    loadDataWithBaseURL(EMBED_ORIGIN + "/", html, "text/html", "utf-8", null)
                }
            },
            onRelease = { it.stopLoading(); it.destroy() },
            modifier = modifier,
        )
    }

    /** https origin hosting the embed page. Any real https site that is not youtube.com works; this is the app's own domain. */
    private const val EMBED_ORIGIN = "https://api.topgunapp.com"
}
