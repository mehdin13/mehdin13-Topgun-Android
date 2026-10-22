// ANDROID-ONLY
package app.topgun.android.platform.ui

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
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

    /** Plays a youtube.com / youtu.be link in the embedded player. The base URL is youtube.com so the embed accepts the request. */
    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun YouTube(url: String, modifier: Modifier) {
        val id = remember(url) { youTubeId(url) }
        if (id == null) {
            Placeholder.Empty(modifier)
            return
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = true
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    val html = """<html><body style="margin:0;background:#000">
                        <iframe width="100%" height="100%" src="https://www.youtube.com/embed/$id?playsinline=1&rel=0" frameborder="0"
                        allow="accelerometer; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe></body></html>"""
                    loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
                }
            },
            onRelease = { it.destroy() },
            modifier = modifier,
        )
    }

    /** Video id from `watch?v=ID`, `youtu.be/ID`, `/embed/ID` or `/shorts/ID`; null when the link is not a YouTube link. */
    internal fun youTubeId(url: String): String? {
        val patterns = listOf(
            Regex("[?&]v=([A-Za-z0-9_-]{11})"),
            Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
            Regex("/(?:embed|shorts|v)/([A-Za-z0-9_-]{11})"),
        )
        return patterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.get(1) }
    }
}
