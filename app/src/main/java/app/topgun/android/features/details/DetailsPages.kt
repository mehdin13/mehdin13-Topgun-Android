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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import app.topgun.android.designsystem.components.PageCounterPill
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ModelOfflineCard
import app.topgun.android.designsystem.components.TGLoadingImage
import app.topgun.android.platform.ui.LocalPlatformUi
import app.topgun.core.model.AircraftDetail
import app.topgun.core.model.ImageGalleryItem
import kotlinx.coroutines.launch

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
        TGLoadingImage(path, contentDescription = description, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
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
        if (!hidden || !showChrome) {
            Box(Modifier.align(Alignment.TopCenter).statusBarsPadding().height(TGSize.tapMin + TGSpace.s * 2), contentAlignment = Alignment.Center) {
                PageCounterPill(pager.currentPage, images.size)
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
                    TGLoadingImage(
                        img.image, contentDescription = "Photo ${i + 1}",
                        modifier = Modifier.size(TGDetailSize.thumbWidth, TGDetailSize.thumbHeight).clip(TGDetailSize.thumbRadius)
                            .border(if (selected) TGDetailSpace.hair else TGDetailSpace.zero, TGDetailColors.onPhoto, TGDetailSize.thumbRadius)
                            .clickable(role = Role.Button) { scope.launch { pager.animateScrollToPage(i) } },
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------- specs

private val specIcons = listOf(Icons.Filled.Eco, Icons.Filled.Settings, Icons.Filled.Build, Icons.Filled.Bolt)

@Composable
private fun SpecSection(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        androidx.compose.material3.HorizontalDivider(color = TGBrand.gray.copy(alpha = 0.4f))
        Text(title, style = TGText.sailecBold(20), color = TG.colors.textPrimary)
    }
}

/** iOS `SettingItem`. [onClick] makes the row a link. */
@Composable
private fun SpecRow(index: Int, title: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = TGSpace.s, horizontal = TGSpace.xs),
        horizontalArrangement = Arrangement.spacedBy(TGSpace.l),
    ) {
        Icon(specIcons[index % specIcons.size], contentDescription = null, tint = TG.colors.textPrimary.copy(alpha = 0.5f), modifier = Modifier.size(TGDetailSize.specIcon))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TGSpace.xs)) {
            Text(title, style = TGText.sailecBold(16), color = TG.colors.textPrimary)
            if (value.isNotEmpty()) Text(value, style = TGText.sailecRegular(13), color = if (onClick != null) TG.colors.accent else TG.colors.textPrimary.copy(alpha = 0.5f))
        }
    }
}

/** iOS `SpecialDetailView`: generation, name, NATO name, sketches and the four specification tables, in the chosen unit system. */
@Composable
private fun SpecsPage(d: AircraftDetail, a: DetailActions, onClose: () -> Unit) {
    var units by rememberSaveable { mutableStateOf(UnitSystem.Metric) }
    val f = remember(units) { SpecFormat(units) }
    val na = SpecFormat.NA
    var n = 0
    fun next() = n++
    Column(Modifier.fillMaxSize().background(TG.colors.background).statusBarsPadding().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(TGSpace.s), verticalAlignment = Alignment.CenterVertically) {
            DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClose)
            Spacer(Modifier.weight(1f))
            UnitsToggle(units) { units = it }
        }
        Column(Modifier.padding(horizontal = TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
            Column(verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                Text(
                    (d.generation ?: na).uppercase(), style = TGText.sailecMedium(10), color = TG.colors.textPrimary,
                    modifier = Modifier.background(TGBrand.red, androidx.compose.foundation.shape.CircleShape).padding(horizontal = TGSpace.s, vertical = TGSpace.xs),
                )
                Text(d.name, style = TGText.sailecBold(28), color = TG.colors.textPrimary)
                Text("Nato Name: ${d.natoReportingName ?: na}", style = TGText.sailecRegular(10), color = TGBrand.placeholder)
            }
            if (d.sketches.isNotEmpty()) {
                val pager = rememberPagerState(pageCount = { d.sketches.size })
                HorizontalPager(pager, Modifier.fillMaxWidth().height(TGDetailSize.sketchHeight)) { i ->
                    TGLoadingImage(d.sketches[i].file, contentDescription = d.sketches[i].name.ifBlank { "Sketch ${i + 1}" }, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                }
            }
            SpecSection("General Specifications:")
            SpecRow(next(), "Role", d.role ?: na)
            SpecRow(next(), "Primary User", d.primaryUser ?: na)
            SpecRow(next(), "Developed From", d.developedFrom?.let { "Aircraft #$it" } ?: na, d.developedFrom?.let { id -> { a.onOpenAircraft(id) } })

            SpecSection("Technical Details:")
            SpecRow(next(), "Length", f.meters(d.length))
            SpecRow(next(), "Height", f.meters(d.height))
            SpecRow(next(), "Wingspan", f.meters(d.wingspan))
            SpecRow(next(), "Full Capacity", f.liters(d.fuelCapacity))
            SpecRow(next(), "Empty Weight", f.kg(d.emptyWeight))
            SpecRow(next(), "Max Weight", f.kg(d.maxWeight))

            SpecSection("Performance Specifications:")
            SpecRow(next(), "Max Speed", f.kph(d.maxSpeed))
            SpecRow(next(), "Range", f.km(d.range))
            SpecRow(next(), "Combat Radius", f.km(d.combatRadius))
            SpecRow(next(), "Ferry Range", f.km(d.ferryRange))
            SpecRow(next(), "Operational Range", f.km(d.operationalRange))
            SpecRow(next(), "Radar Range", f.km(d.radarRange))
            SpecRow(next(), "Trust-to-Weight Ratio", d.trustToWeightRatio ?: na)
            SpecRow(next(), "Max G-Load(Gs)", d.maxGLoadGs ?: na)

            SpecSection("Other Specifications:")
            SpecRow(next(), "Wing Configuration", d.wingConfiguration ?: na)
            SpecRow(next(), "Radar System", d.radarSystem ?: na)
            SpecRow(next(), "Jamming Systems", d.jammingSystems ?: na)
            SpecRow(next(), "Engine Type", d.engineType ?: na)
            SpecRow(next(), "Armament (Guns)", d.armament ?: na)
            SpecRow(next(), "Canopy Type", d.canopyType ?: na)
            SpecRow(next(), "IRST", d.irst ?: na)

            // Fields the iOS screen does not show, and the category's custom fields. Only filled values are listed.
            val extras = listOfNotNull(
                d.aircraftClass?.let { "Class" to it },
                d.numberOfEngines?.let { "Number of Engines" to it.toString() },
                d.engineAfterburner?.let { "Afterburner" to if (it) "Yes" else "No" },
                d.flyByWire?.let { "Fly-by-Wire" to if (it) "Yes" else "No" },
                d.hardpoints?.let { "Hardpoints" to it.toString() },
                d.wingArea?.let { "Wing Area" to it },
                d.wingLoading?.let { "Wing Loading" to it },
                d.grossWeight?.let { "Gross Weight" to it },
                d.serviceCeiling?.let { "Service Ceiling" to it.toString() },
                d.rateOfClimb?.let { "Rate of Climb" to it.toString() },
                d.radarCrossSection?.let { "Radar Cross Section" to it.toString() },
                d.stealthCapabilities?.let { "Stealth" to it },
                d.electronicCountermeasures?.let { "Electronic Countermeasures" to it },
                d.propulsion?.let { "Propulsion" to it },
            ).filter { it.second.isNotBlank() } + d.customFields.mapNotNull { c ->
                val name = c.fieldName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                name to (c.fieldValue ?: "")
            }
            if (extras.isNotEmpty()) {
                SpecSection("More Specifications:")
                extras.forEach { (title, value) -> SpecRow(next(), title, value) }
            }
            Spacer(Modifier.height(TGDetailSpace.bottomClear))
        }
    }
}

@Composable
private fun UnitsToggle(selected: UnitSystem, onSelect: (UnitSystem) -> Unit) {
    Row(Modifier.clip(TGRadius.button).background(TG.colors.card)) {
        UnitSystem.entries.forEach { u ->
            Text(
                u.label, style = TGText.sailecMedium(14),
                color = if (u == selected) TGDetailColors.onPhoto else TG.colors.textPrimary,
                modifier = Modifier.background(if (u == selected) TG.colors.accent else TG.colors.card)
                    .defaultMinSize(minHeight = TGSize.tapMin).clickable(role = Role.Button) { onSelect(u) }
                    .padding(horizontal = TGSpace.m).wrapContentHeightCenter(),
            )
        }
    }
}

private fun Modifier.wrapContentHeightCenter(): Modifier = this.then(Modifier.wrapContentHeight(Alignment.CenterVertically))

// ------------------------------------------------------------- videos

/** iOS `VideoPlayerView`: the gallery videos one after another with a close and a next button. */
@Composable
private fun VideosPage(d: AircraftDetail, onClose: () -> Unit) {
    val media = LocalAppContainer.current.media
    val urls = remember(d.videoGallery) { d.videoGallery.mapNotNull { media.resolve(it.video) } }
    var index by rememberSaveable { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().background(TGDetailColors.photoBlack).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(TGSpace.s), verticalAlignment = Alignment.CenterVertically) {
            DetailCircleButton(Icons.Filled.Close, "Close", onClose, glyph = TGDetailColors.onPhoto, background = TGDetailColors.photoBlack.copy(alpha = 0.4f))
            Spacer(Modifier.weight(1f))
            if (urls.size > 1) Text("${index + 1} / ${urls.size}", style = TGText.sailecMedium(14), color = TGDetailColors.onPhoto)
            Spacer(Modifier.weight(1f))
            if (index < urls.size - 1) {
                DetailCircleButton(Icons.Filled.SkipNext, "Next video", { index += 1 }, glyph = TGDetailColors.onPhoto, background = TGDetailColors.photoBlack.copy(alpha = 0.4f))
            } else {
                Spacer(Modifier.size(TGSize.tapMin))
            }
        }
        if (urls.isEmpty()) {
            Text("Error loading videos.", style = TGText.sailecMedium(16), color = TGBrand.red, modifier = Modifier.padding(TGSpace.l))
        } else {
            key(urls[index.coerceIn(0, urls.size - 1)]) {
                LocalPlatformUi.current.VideoPlayer(urls[index.coerceIn(0, urls.size - 1)], Modifier.fillMaxWidth().weight(1f))
            }
        }
    }
}

// ----------------------------------------------------------- operators

/** iOS `MapView`: the full map of the countries that operate the aircraft. A pin opens the country. */
@Composable
private fun OperatorsPage(d: AircraftDetail, a: DetailActions, onClose: () -> Unit) {
    val pins = rememberOperatorPins(d.countriesOfOperation)
    Box(Modifier.fillMaxSize()) {
        LocalPlatformUi.current.WorldMap(pins, Modifier.fillMaxSize(), onPinClick = { a.onOpenCountry(it.id) })
        DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClose, Modifier.statusBarsPadding().padding(TGSpace.s))
    }
}

// ----------------------------------------------------------------- pdf

/** iOS `AircraftPdf`. */
@Composable
private fun PdfPage(d: AircraftDetail, onClose: () -> Unit) {
    val url = LocalAppContainer.current.media.resolve(d.filePdf)
    Column(Modifier.fillMaxSize().background(TG.colors.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(TGSpace.s), verticalAlignment = Alignment.CenterVertically) {
            DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClose)
            Text("${d.name} PDF", style = TGText.sailecBold(16), color = TG.colors.textPrimary, modifier = Modifier.padding(start = TGSpace.m), maxLines = 1)
        }
        if (url == null) {
            Text("Failed to load PDF.", style = TGText.sailecMedium(16), color = TG.colors.textPrimary, modifier = Modifier.padding(TGSpace.l))
        } else {
            LocalPlatformUi.current.PdfViewer(url, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

// --------------------------------------------------------------- 3D/AR

/** Google Scene Viewer deep link (opens the Scene Viewer / browser handler; needs an https file URL). */
private fun sceneViewerUrl(glbUrl: String, title: String): String =
    "https://arvr.google.com/scene-viewer/1.0?file=" + java.net.URLEncoder.encode(glbUrl, "UTF-8") +
        "&mode=3d_preferred&title=" + java.net.URLEncoder.encode(title, "UTF-8")

private fun isGltf(url: String?): Boolean {
    val path = url?.substringBefore('?')?.lowercase() ?: return false
    return path.endsWith(".glb") || path.endsWith(".gltf")
}

/**
 * iOS `Model3DView`. The backend ships `.usdz` (Apple only) in `file_3D` and a converted `.glb` in `file_3D_glb`;
 * the Android viewer and AR use the glb (or a gltf) and fall back to the "not available" card when there is none.
 * The viewer shows the real download progress and keeps the file (filesDir/models); [ModelOfflineCard] lets the person
 * download it ahead of time for offline use and remove it again. Settings > Clear Cache removes all models.
 * "Open in 3D viewer" hands the same file to Google's Scene Viewer if the in-app viewer cannot show it.
 */
@Composable
private fun ModelPage(d: AircraftDetail, onClose: () -> Unit) {
    val media = LocalAppContainer.current.media
    val platform = LocalAppContainer.current.platform
    val url = remember(d.fileGlb, d.file3d) {
        listOf(d.fileGlb, d.file3d).firstNotNullOfOrNull { candidate -> media.resolve(candidate)?.takeIf { isGltf(it) } }
    }
    val supported = url != null
    var ar by rememberSaveable { mutableStateOf(false) }
    if (ar && url != null) {
        LocalPlatformUi.current.ArViewer(url, Modifier.fillMaxSize(), onClose = { ar = false })
        return
    }
    Column(Modifier.fillMaxSize().background(TG.colors.background).statusBarsPadding().padding(horizontal = TGDetailSpace.ten)) {
        Row(Modifier.fillMaxWidth().padding(vertical = TGSpace.s)) { DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClose) }
        if (supported && url != null) {
            LocalPlatformUi.current.Model3dViewer(url, Modifier.fillMaxWidth().height(TGDetailSize.modelHeight))
        } else {
            Column(
                Modifier.fillMaxWidth().height(TGDetailSize.modelHeight).clip(TGRadius.card).background(TG.colors.card).padding(TGSpace.xl),
                verticalArrangement = Arrangement.spacedBy(TGSpace.m, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Filled.ViewInAr, contentDescription = null, tint = TG.colors.textSecondary, modifier = Modifier.size(TGSize.tapMin))
                Text("3D model not available on Android yet", style = TGText.sailecBold(16), color = TG.colors.textPrimary, textAlign = TextAlign.Center)
                Text("Only the Apple (usdz) version of this model exists so far; there is no .glb file for Android to download. We are working on it.", style = TGText.sailecRegular(13), color = TG.colors.textSecondary, textAlign = TextAlign.Center)
            }
        }
        // Only this part scrolls: the 3D viewer above must keep its vertical drag for orbiting, the buttons below stay pinned.
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = TGSpace.xl),
            verticalArrangement = Arrangement.spacedBy(TGSpace.m),
        ) {
            Text(d.name, style = TGText.sailecBold(28), color = TG.colors.textPrimary, maxLines = 2)
            d.role?.takeIf { it.isNotBlank() }?.let { Text(it, style = TGText.sailecMedium(16), color = TGBrand.placeholder) }
            if (url != null) {
                val downloads = platform.modelDownloads
                val downloadState by remember(url) { downloads.state(url) }.collectAsStateWithLifecycle()
                ModelOfflineCard(
                    state = downloadState,
                    onDownload = { downloads.download(url) },
                    onCancel = { downloads.cancel(url) },
                    onRemove = { downloads.remove(url) },
                    modifier = Modifier.padding(top = TGSpace.s),
                )
            }
        }
        if (url != null) {
            OutlinedButton(
                onClick = { platform.openUrl(sceneViewerUrl(url, d.name)) },
                modifier = Modifier.align(Alignment.CenterHorizontally).width(TGDetailSize.modelButtonWidth).defaultMinSize(minHeight = TGSize.tapMin),
                shape = TGDetailSize.buttonRadius,
            ) {
                Text("Open in 3D viewer", style = TGText.sailecMedium(16))
            }
            Spacer(Modifier.height(TGSpace.s))
            Button(
                onClick = { ar = true },
                modifier = Modifier.align(Alignment.CenterHorizontally).width(TGDetailSize.modelButtonWidth).defaultMinSize(minHeight = TGSize.tapMin),
                shape = TGDetailSize.buttonRadius,
            ) {
                Icon(Icons.Filled.ViewInAr, contentDescription = null)
                Text("View in AR", style = TGText.sailecMedium(16), modifier = Modifier.padding(start = TGSpace.s))
            }
        }
        Spacer(Modifier.height(TGSpace.xl).navigationBarsPadding())
    }
}
