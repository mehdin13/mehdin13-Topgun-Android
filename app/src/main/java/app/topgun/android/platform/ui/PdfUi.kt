// ANDROID-ONLY
package app.topgun.android.platform.ui

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.topgun.android.platform.ModelCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Downloads the PDF to `cacheDir/pdf` and draws its pages with [PdfRenderer] in a scrolling list. */
internal object PdfUi {
    @Composable
    fun Viewer(url: String, modifier: Modifier) {
        val context = LocalContext.current
        var file by remember(url) { mutableStateOf<File?>(null) }
        var failed by remember(url) { mutableStateOf(false) }
        LaunchedEffect(url) {
            val result = ModelCache.downloadToCache(File(context.cacheDir, "pdf"), url)
            file = result
            failed = result == null
        }
        Box(modifier, contentAlignment = Alignment.Center) {
            val pdf = file
            when {
                failed -> Text(stringResource(R.string.pdf_load_failed))
                pdf == null -> CircularProgressIndicator()
                else -> Pages(pdf, Modifier.fillMaxSize())
            }
        }
    }

    @Composable
    private fun Pages(file: File, modifier: Modifier) {
        val holder = remember(file) {
            runCatching {
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                fd to PdfRenderer(fd)
            }.getOrNull()
        }
        DisposableEffect(holder) {
            onDispose { holder?.let { runCatching { it.second.close(); it.first.close() } } }
        }
        if (holder == null) {
            Text(stringResource(R.string.pdf_load_failed))
            return
        }
        val renderer = holder.second
        val lock = remember(renderer) { Mutex() } // PdfRenderer allows one open page at a time
        BoxWithConstraints(modifier) {
            val widthPx = with(LocalDensity.current) { maxWidth.roundToPx() }.coerceIn(1, 1600)
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items((0 until renderer.pageCount).toList()) { index ->
                    var bitmap by remember(index, widthPx) { mutableStateOf<Bitmap?>(null) }
                    LaunchedEffect(index, widthPx) {
                        bitmap = withContext(Dispatchers.IO) {
                            lock.withLock {
                                renderer.openPage(index).use { page ->
                                    val height = (widthPx * page.height.toFloat() / page.width).toInt().coerceAtLeast(1)
                                    Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888).also {
                                        it.eraseColor(Color.WHITE)
                                        page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    }
                                }
                            }
                        }
                    }
                    val b = bitmap
                    if (b != null) {
                        Image(b.asImageBitmap(), contentDescription = stringResource(R.string.pdf_page_cd, index + 1, renderer.pageCount), modifier = Modifier.fillMaxWidth().aspectRatio(b.width.toFloat() / b.height))
                    } else {
                        Box(Modifier.fillMaxWidth().aspectRatio(0.75f).padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}
