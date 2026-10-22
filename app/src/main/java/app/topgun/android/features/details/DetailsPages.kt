package app.topgun.android.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.data.UserState
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGDetailSpace
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.DetailCircleButton
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.TGImage
import app.topgun.android.platform.ui.LocalPlatformUi
import app.topgun.core.model.AircraftDetail
import app.topgun.core.model.ImageGalleryItem
import androidx.compose.material.icons.Icons.Filled

/** Routes a [DetailPage] to its composable. [onClose] returns to the detail. */
@Composable
internal fun DetailSubPage(page: DetailPage, arg: Int, d: AircraftDetail, ui: DetailsState, user: UserState, a: DetailActions, onClose: () -> Unit) {
    when (page) {
        DetailPage.Main -> Unit
        DetailPage.Gallery -> GalleryPage(d.imageGallery, 0, showChrome = true, onClose = onClose)
        DetailPage.DeckViewer -> GalleryPage(d.imageGallery.filter { it.isPortrait }, arg, showChrome = false, onClose = onClose)
        DetailPage.Specs -> SpecsPage(d, a, onClose)
        DetailPage.Videos -> VideosPage(d, onClose)
        DetailPage.Operators -> OperatorsPage(d, a, onClose)
        DetailPage.Pdf -> PdfPage(d, onClose)
        DetailPage.Model -> ModelPage(d, onClose)
        DetailPage.Comments -> CommentsPage(d.id, ui.averageRating, user, a, onClose)
    }
}

// ------------------------------------------------------------- gallery

/** Pinch to zoom (1x..4x), drag to pan when zoomed, double tap to toggle 2x. Single-finger drags at 1x pass through to the pager. */
@Composable
private fun ZoomableImage(path: String, description: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier.fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { if (scale > 1f) { scale = 1f; offset = Offset.Zero } else scale = 2f })
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        if (event.changes.size > 1 || scale > 1f) {
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            offset = if (scale > 1f) offset + pan else Offset.Zero
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed && !it.changedToUp() })
                }
            }
            .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
    ) {
        TGImage(path, contentDescription = description, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
    }
}

/**
 * iOS `GalleryView` ([showChrome] true: title, thumbnails, tap to hide the chrome) and the card deck's
 * `FullScreenGalleryView` ([showChrome] false: just a close button). Saving to Photos is not ported (see docs/reports/details.md).
 */
@Composable
private fun GalleryPage(images: List<ImageGalleryItem>, start: Int, showChrome: Boolean, onClose: () -> Unit) {
    if (images.isEmpty()) {
        Box(Modifier.fillMaxSize().background(TGDetailColors.photoBlack).statusBarsPadding()) {
            DetailCircleButton(Icons.Filled.Close, "Close", onClose, Modifier.padding(TGSpace.s))
            EmptyState("No images available.", Modifier.align(Alignment.Center))
        }
        return
    }
    val pager = rememberPagerState(initialPage = start.coerceIn(0, images.size - 1), pageCount = { images.size })
    var hidden by rememberSaveable { mutableStateOf(false) }
    val thumbs = rememberLazyListState()
    LaunchedEffect(pager.currentPage) { thumbs.animateScrollToItem(pager.currentPage) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(TGDetailColors.photoBlack)) {
        HorizontalPager(pager, Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { hidden = !hidden }) { i ->
            ZoomableImage(images[i].image, "Aircraft photo ${i + 1} of ${images.size}")
        }
        if (!hidden || !showChrome) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = TGSpace.s, vertical = TGSpace.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showChrome) {
                    DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClose)
                    Text("Gallery", style = TGText.sailecBold(22), color = TGDetailColors.onPhoto, modifier = Modifier.padding(start = TGSpace.m))
                } else {
                    Spacer(Modifier.weight(1f))
                    DetailCircleButton(Icons.Filled.Close, "Close", onClose, glyph = TGDetailColors.onPhoto, background = TGDetailColors.photoBlack.copy(alpha = 0.4f))
                }
            }
        }
        if (showChrome && !hidden) {
            LazyRow(
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = TGSpace.s, vertical = TGSpace.l)
                    .background(TGDetailColors.photoBlack.copy(alpha = 0.6f), TGDetailSize.thumbRadius),
                state = thumbs,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(TGSpace.m),
                horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
            ) {
                itemsIndexed(images) { i, img ->
                    val selected = i == pager.currentPage
                    TGImage(
                        img.image, contentDescription = "Photo ${i + 1}",
                        modifier = Modifier.size(TGDetailSize.thumbWidth, TGDetailSize.thumbHeight).clip(TGDetailSize.thumbRadius)
                            .border(if (selected) TGDetailSpace.hair else TGDetailSpace.zero, TGDetailColors.onPhoto, TGDetailSize.thumbRadius)
                            .clickable(role = Role.Button) { scope.launchScroll(pager, i) },
                    )
                }
            }
        }
    }
}

private fun kotlinx.coroutines.CoroutineScope.launchScroll(pager: androidx.compose.foundation.pager.PagerState, index: Int) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).let { }
    this.launchIn { pager.animateScrollToPage(index) }
}

private fun kotlinx.coroutines.CoroutineScope.launchIn(block: suspend () -> Unit) {
    kotlinx.coroutines.launch(this) { block() }
}
