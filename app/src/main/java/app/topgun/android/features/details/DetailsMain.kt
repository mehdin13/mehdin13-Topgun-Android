package app.topgun.android.features.details

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.delay
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import app.topgun.android.R
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.UserState
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAsset
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGDetailColors
import app.topgun.android.designsystem.TGDetailSize
import app.topgun.android.designsystem.TGDetailSpace
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AircraftListCard
import app.topgun.android.designsystem.components.DetailActionTile
import app.topgun.android.designsystem.components.DetailCircleButton
import app.topgun.android.designsystem.components.SlidingDotsIndicator
import app.topgun.android.platform.ui.youTubeVideoId
import app.topgun.android.designsystem.components.DetailHeading
import app.topgun.android.designsystem.components.DetailInfoCell
import app.topgun.android.designsystem.components.DetailPill
import app.topgun.android.designsystem.components.DetailStars
import app.topgun.android.designsystem.components.TGImage
import app.topgun.android.designsystem.components.TGLoadingImage
import app.topgun.android.platform.ui.LocalPlatformUi
import app.topgun.android.platform.ui.MapPin
import app.topgun.core.model.AircraftDetail
import app.topgun.core.model.CountryOfOperation
import app.topgun.core.model.ImageGalleryItem
import app.topgun.core.model.ManufacturerBrief

/** The scrolling detail page (iOS `DetailsView` body). */
@Composable
internal fun DetailMain(d: AircraftDetail, ui: DetailsState, user: UserState, a: DetailActions) {
    val platform = LocalAppContainer.current.platform
    var showRatingAlert by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shareMessage = stringResource(R.string.details_share_text, d.name)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailHeader(
            imagePath = d.thumbnail ?: d.mainImage,
            name = d.name,
            isFavorite = ui.isFavorite,
            onBack = a.onBack,
            onShare = { platform.share(shareMessage) },
            onFavorite = a.onFavorite,
        )
        Column(
            Modifier.fillMaxWidth().padding(horizontal = TGSpace.l).padding(top = TGSpace.s),
            verticalArrangement = Arrangement.spacedBy(TGSpace.l),
        ) {
            TitleBlock(d, ui, a.onLike)
            DetailDivider()
            QuickActions(d, a)
            DetailDivider()
            Overview(d.description, expanded) { expanded = !expanded }
            if (!d.trivia.isNullOrBlank()) {
                DetailDivider()
                Column(verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
                    DetailHeading(stringResource(R.string.details_trivia), R.drawable.update, Modifier.fillMaxWidth())
                    Text(d.trivia.orEmpty(), style = TGText.sailecRegular(17), color = TG.colors.textPrimary, modifier = Modifier.padding(horizontal = TGDetailSpace.five))
                }
            }
            DetailDivider()
            GeneralDetails(d)
            if (d.imageGallery.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
                    DetailHeading(stringResource(R.string.details_gallery), R.drawable.card, Modifier.fillMaxWidth())
                    CardDeck(d.imageGallery.filter { it.isPortrait }, onOpen = { a.open(DetailPage.DeckViewer, it) })
                    DetailDivider()
                }
            }
            VideoSection(d.youtubeVideo)
            DetailDivider()
            if (d.countriesOfOperation.isNotEmpty()) {
                Operators(d.countriesOfOperation, a)
                DetailDivider()
            }
            if (d.manufacturers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
                    DetailHeading(stringResource(R.string.details_manufacturer), R.drawable.direction, Modifier.fillMaxWidth())
                    d.manufacturers.forEach { ManufacturerRow(it) { a.onOpenManufacturer(it.id) } }
                }
                DetailDivider()
            }
            if (d.variants.isNotEmpty()) {
                AircraftRail(stringResource(R.string.details_variants), d, d.variants, user, a)
                DetailDivider()
            }
            if (d.similar.isNotEmpty()) {
                AircraftRail(stringResource(R.string.details_similar), d, d.similar, user, a)
                DetailDivider()
            }
            d.wikipediaLink?.takeIf { it.isNotBlank() }?.let { link ->
                Column(verticalArrangement = Arrangement.spacedBy(TGSpace.l), modifier = Modifier.padding(vertical = TGSpace.l)) {
                    DetailHeading(stringResource(R.string.details_learn_more), R.drawable.direction, Modifier.fillMaxWidth())
                    Row(
                        Modifier.fillMaxWidth().defaultMinSize(minHeight = TGSize.tapMin).clip(TGRadius.card).background(TG.colors.card)
                            .clickable(role = Role.Button) { platform.openUrl(link) }.padding(TGSpace.m),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
                    ) {
                        Icon(Icons.Outlined.Language, contentDescription = null, tint = TG.colors.accent)
                        Text(stringResource(R.string.details_wikipedia), style = TGText.sailecMedium(16), color = TG.colors.accent, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = stringResource(R.string.details_opens_in_browser), tint = TG.colors.accent)
                    }
                }
            }
            RatingSection(ui, onRate = { if (user.isLoggedIn) a.onRate(it) else showRatingAlert = true })
            Box(
                Modifier.fillMaxWidth().padding(vertical = TGSpace.xl, horizontal = TGDetailSpace.ten).height(TGSize.tapMin + TGDetailSpace.hair)
                    .clip(TGDetailSize.cellRadius).background(TGBrand.yellow)
                    .clickable(role = Role.Button) { a.open(DetailPage.Comments, 0) },
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.details_show_comments), style = TGText.sailecMedium(16), color = TGDetailColors.photoBlack)
            }
        }
        Spacer(Modifier.height(TGDetailSpace.bottomClear))
    }

    if (showRatingAlert) {
        AlertDialog(
            onDismissRequest = { showRatingAlert = false },
            title = { Text(stringResource(R.string.ds_sign_in_required), style = TGText.sailecBold(18)) },
            text = { Text(stringResource(R.string.details_sign_in_to_rate), style = TGText.sailecRegular(14)) },
            confirmButton = { TextButton({ showRatingAlert = false; a.onShowSignIn() }) { Text(stringResource(R.string.ds_sign_in)) } },
            dismissButton = { TextButton({ showRatingAlert = false }) { Text(stringResource(R.string.ds_ok)) } },
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(color = TGBrand.gray.copy(alpha = 0.4f))
}

// ---------------------------------------------------------------- header

/** iOS `AircraftHeaderView`: 400pt image with back, share and favorite circles on top. */
@Composable
private fun DetailHeader(imagePath: String?, name: String, isFavorite: Boolean, onBack: () -> Unit, onShare: () -> Unit, onFavorite: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(TGDetailSize.headerHeight)) {
        if (imagePath.isNullOrBlank()) {
            Image(painterResource(R.drawable.img_0_placeholder), contentDescription = stringResource(R.string.ds_photo_cd, name), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            TGImage(imagePath, contentDescription = stringResource(R.string.ds_photo_cd, name), modifier = Modifier.fillMaxSize())
        }
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = TGDetailSpace.headerButtonSide - TGSpace.s).padding(top = TGDetailSpace.headerButtonTop),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.xs),
        ) {
            DetailCircleButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ds_back), onBack)
            Spacer(Modifier.weight(1f))
            DetailCircleButton(Icons.Filled.Share, stringResource(R.string.details_share_cd, name), onShare)
            DetailCircleButton(
                if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                if (isFavorite) stringResource(R.string.ds_remove_from_favorites_cd, name) else stringResource(R.string.details_add_favorite_cd, name),
                onFavorite,
                glyph = if (isFavorite) TGBrand.red else TGDetailColors.photoBlack,
            )
        }
    }
}

/** iOS `AircraftTitleView`, plus the like button. */
@Composable
private fun TitleBlock(d: AircraftDetail, ui: DetailsState, onLike: () -> Unit) {
    val likeDescription = stringResource(if (ui.isLiked) R.string.details_unlike_cd else R.string.details_like_cd, ui.likesCount)
    Column(Modifier.padding(vertical = TGSpace.s), verticalArrangement = Arrangement.spacedBy(TGDetailSpace.ten)) {
        Text(
            d.name, style = TGText.sailecBold(22), color = TG.colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = TGDetailSpace.five),
        )
        Row(Modifier.padding(horizontal = TGDetailSpace.five), horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            d.nationalOrigin?.takeIf { it.isNotEmpty() }?.let { DetailPill(it, Icons.Filled.Public, TGBrand.blue, Modifier.weight(1f, fill = false)) }
            d.generation?.takeIf { it.isNotEmpty() }?.let { DetailPill(it, Icons.Filled.Bolt, TGBrand.red, Modifier.weight(1f, fill = false)) }
        }
        d.manufacturers.firstOrNull()?.name?.takeIf { it.isNotEmpty() }?.let {
            Row(Modifier.padding(horizontal = TGDetailSpace.five)) {
                DetailPill(it, Icons.Filled.Build, TGDetailColors.manufacturerGreen, tintAlpha = 0.15f)
            }
        }
        Row(
            Modifier.padding(horizontal = TGDetailSpace.five).defaultMinSize(minHeight = TGSize.tapMin).clip(TGDetailSize.pill)
                .clickable(role = Role.Button, onClick = onLike)
                .semantics { contentDescription = likeDescription },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
        ) {
            Icon(if (ui.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp, contentDescription = null, tint = if (ui.isLiked) TG.colors.accent else TG.colors.textSecondary)
            Text("${ui.likesCount}", style = TGText.sailecMedium(14), color = TG.colors.textPrimary)
        }
    }
}

// --------------------------------------------------------------- tiles

/** iOS `QuickActionsGrid`: two tiles per row. Tiles whose content is empty are left out. */
@Composable
private fun QuickActions(d: AircraftDetail, a: DetailActions) {
    class Tile(val icon: androidx.compose.ui.graphics.vector.ImageVector, val text: String, val subtitle: String, val active: Boolean, val gradient: List<androidx.compose.ui.graphics.Color>, val page: DetailPage)
    val tiles = buildList {
        if (d.imageGallery.isNotEmpty()) add(Tile(Icons.Filled.PhotoLibrary, stringResource(R.string.details_gallery), "", true, TGDetailColors.tileBlue, DetailPage.Gallery))
        add(Tile(Icons.Filled.AutoAwesome, stringResource(R.string.details_specification), stringResource(R.string.details_more_details), true, TGDetailColors.tileOrange, DetailPage.Specs))
        if (d.videoGallery.isNotEmpty()) add(Tile(Icons.Filled.VideoLibrary, stringResource(R.string.details_video_gallery), pluralStringResource(R.plurals.details_videos_count, d.videoGallery.size, d.videoGallery.size), false, TGDetailColors.tileOrange, DetailPage.Videos))
        if (d.countriesOfOperation.isNotEmpty()) add(Tile(Icons.Filled.Shield, stringResource(R.string.details_operators), stringResource(R.string.details_country_operation), true, TGDetailColors.tilePink, DetailPage.Operators))
        if (!d.filePdf.isNullOrBlank()) add(Tile(Icons.Filled.PictureAsPdf, stringResource(R.string.details_pdf), "", true, TGDetailColors.tileCherry, DetailPage.Pdf))
        if (!d.file3d.isNullOrBlank() || !d.fileGlb.isNullOrBlank()) add(Tile(Icons.Filled.ViewInAr, stringResource(R.string.details_3d_model), "", true, TGDetailColors.tileGreen, DetailPage.Model))
    }
    Column(verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                row.forEach { t ->
                    DetailActionTile(
                        t.icon, t.text, onClick = { a.open(t.page, 0) }, modifier = Modifier.weight(1f), subtitle = t.subtitle,
                        gradient = if (t.active) t.gradient else TGDetailColors.tileInactive,
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ------------------------------------------------------------ overview

/** iOS `OverviewSection`: first paragraph (or 800 characters) with Show More / Show Less. */
@Composable
private fun Overview(description: String?, expanded: Boolean, onToggle: () -> Unit) {
    Column(Modifier.padding(vertical = TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
        DetailHeading(stringResource(R.string.details_overview), R.drawable.update, Modifier.fillMaxWidth())
        val full = description.orEmpty()
        if (full.isNotEmpty()) {
            val threshold = 800
            val first = full.split("\n").firstOrNull { it.isNotEmpty() }.orEmpty()
            val shouldTruncate = full.length > threshold || first.length < full.length
            val preview = if (first.length < threshold) first else full.take(threshold)
            Column(verticalArrangement = Arrangement.spacedBy(TGDetailSpace.six)) {
                Text(if (expanded) full else preview, style = TGText.sailecRegular(17), color = TG.colors.textPrimary, modifier = Modifier.padding(horizontal = TGDetailSpace.five))
                if (shouldTruncate) {
                    Text(
                        stringResource(if (expanded) R.string.details_show_less else R.string.details_show_more),
                        style = TGText.sailecBold(16), color = TG.colors.accent,
                        modifier = Modifier.defaultMinSize(minHeight = TGSize.tapMin).clickable(role = Role.Button, onClick = onToggle).padding(horizontal = TGDetailSpace.five)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralDetails(d: AircraftDetail) {
    Column(Modifier.padding(vertical = TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
        DetailHeading(stringResource(R.string.details_general_details), R.drawable.weapon, Modifier.fillMaxWidth())
        Column(verticalArrangement = Arrangement.spacedBy(TGDetailSpace.five)) {
            val na = stringResource(R.string.ds_not_available)
            InfoRow(
                stringResource(R.string.details_introduction) to (d.introduction ?: na),
                stringResource(R.string.details_first_flight) to (d.firstFlight ?: na),
                stringResource(R.string.details_status) to (d.status ?: na),
            )
            InfoRow(
                stringResource(R.string.details_price) to stringResource(R.string.details_price_million, d.priceMillion?.toString() ?: na),
                stringResource(R.string.details_number_built) to (d.numberBuilt ?: na),
                stringResource(R.string.details_crew_size) to "${d.crewSize ?: 1}",
            )
        }
    }
}

@Composable
private fun InfoRow(vararg cells: Pair<String, String>) {
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        cells.forEach { (title, value) -> DetailInfoCell(title, value, Modifier.weight(1f).fillMaxHeight()) }
    }
}

// ------------------------------------------------------------- gallery

/** iOS `CardDeckExample`: portrait images as peeking cards with a page indicator. Tap opens the full-screen viewer. */
@Composable
private fun CardDeck(images: List<ImageGalleryItem>, onOpen: (Int) -> Unit) {
    if (images.isEmpty()) {
        Text(stringResource(R.string.details_no_portrait_images), style = TGText.sailecRegular(14), color = TGBrand.placeholder)
        return
    }
    val pager = rememberPagerState(pageCount = { images.size })
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = TGDetailSize.deckPeek),
            pageSpacing = TGSpace.m,
        ) { index ->
            TGLoadingImage(
                images[index].image, contentDescription = stringResource(R.string.details_aircraft_photo_cd, index + 1, images.size),
                modifier = Modifier.fillMaxWidth().aspectRatio(TGDetailSize.deckAspect).clip(TGDetailSize.deckRadius).clickable(role = Role.Button) { onOpen(index) },
            )
        }
        SlidingDotsIndicator(images.size, pager.currentPage + pager.currentPageOffsetFraction)
    }
}

// --------------------------------------------------------------- video

/** A clean watch link for the YouTube app/browser; the stored link when no id can be parsed. */
private fun watchUrl(link: String): String {
    val id = youTubeVideoId(link)
    return if (id != null) "https://www.youtube.com/watch?v=$id" else if (link.contains("://")) link else "https://$link"
}

@Composable
private fun VideoSection(link: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
        DetailHeading(stringResource(R.string.details_video), R.drawable.card, Modifier.fillMaxWidth())
        if (!link.isNullOrBlank()) {
            val platform = LocalAppContainer.current.platform
            LocalPlatformUi.current.YouTubePlayer(link, Modifier.fillMaxWidth().height(TGDetailSize.mediaHeight).clip(TGDetailSize.mediaRadius))
            // Always offered, so people are never stuck when the embed refuses to play.
            OutlinedButton(
                onClick = { platform.openUrl(watchUrl(link)) },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = TGSize.tapMin),
                shape = TGDetailSize.buttonRadius,
            ) { Text(stringResource(R.string.video_watch_on_youtube), style = TGText.sailecMedium(16)) }
        } else {
            Text(stringResource(R.string.details_no_video), style = TGText.sailecRegular(14), color = TGBrand.placeholder)
        }
    }
}

// ----------------------------------------------------------- operators

/** Pins for the countries of operation that have coordinates. */
@Composable
internal fun rememberOperatorPins(countries: List<CountryOfOperation>): List<MapPin> {
    val media = LocalAppContainer.current.media
    val context = LocalContext.current
    val unknown = stringResource(R.string.ds_unknown)
    return remember(countries, unknown) {
        countries.mapNotNull { c ->
            val lat = c.latitude ?: return@mapNotNull null
            val lon = c.longitude ?: return@mapNotNull null
            MapPin(c.id, c.name ?: unknown, context.getString(R.string.details_aircraft_count, c.aircraftCount), lat, lon, media.resolve(c.flagIcon))
        }
    }
}

@Composable
private fun Operators(countries: List<CountryOfOperation>, a: DetailActions) {
    val pins = rememberOperatorPins(countries)
    Column(verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
        DetailHeading(stringResource(R.string.details_operators), R.drawable.building, Modifier.fillMaxWidth())
        FlagGrid(countries)
        if (pins.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().height(TGDetailSize.mediaHeight).clip(TGDetailSize.mediaRadius)) {
                LocalPlatformUi.current.WorldMap(pins, Modifier.fillMaxSize(), onPinClick = { a.onOpenCountry(it.id) })
                DetailCircleButton(
                    Icons.Filled.Info, stringResource(R.string.details_open_full_map), { a.open(DetailPage.Operators, 0) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(TGSpace.xs),
                    glyph = TGDetailColors.onPhoto, background = TGDetailColors.photoBlack.copy(alpha = 0.4f),
                )
            }
        }
    }
}

/**
 * iOS `CountryFlagGrid`: flags in max(4, width / 70) columns. Tapping a flag shows a tooltip with the country name
 * (and its aircraft count) for two seconds; it does not navigate. A pin on the map below opens the country.
 */
@Composable
private fun FlagGrid(countries: List<CountryOfOperation>) {
    var selected by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(selected) {
        if (selected != null) {
            delay(TGDetailSize.tooltipMillis)
            selected = null
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = maxOf(TGDetailSize.flagMinColumns, (maxWidth / TGDetailSize.flagColumnWidth).toInt())
        Column(Modifier.padding(horizontal = TGDetailSpace.five, vertical = TGSpace.s), verticalArrangement = Arrangement.spacedBy(TGDetailSize.flagGap)) {
            countries.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TGDetailSize.flagGap)) {
                    row.forEach { c ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            FlagCell(c) { selected = c.id }
                            if (selected == c.id) FlagTooltip(c)
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Dark bubble above the tapped flag (iOS `tooltipView`). */
@Composable
private fun FlagTooltip(c: CountryOfOperation) {
    val above = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                val x = (anchorBounds.center.x - popupContentSize.width / 2).coerceIn(0, maxOf(0, windowSize.width - popupContentSize.width))
                return IntOffset(x, maxOf(0, anchorBounds.top - popupContentSize.height))
            }
        }
    }
    Popup(popupPositionProvider = above) {
        Column(
            Modifier.background(TGDetailColors.photoBlack.copy(alpha = TGDetailSize.tooltipAlpha), TGDetailSize.tooltipRadius)
                .padding(horizontal = TGSpace.l, vertical = TGSpace.s)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(c.name ?: stringResource(R.string.ds_unknown), style = TGText.sailecBold(12), color = TGDetailColors.onPhoto, maxLines = 1)
            Text(stringResource(R.string.details_aircraft_count, c.aircraftCount), style = TGText.sailecRegular(12), color = TGDetailColors.onPhoto.copy(alpha = 0.8f), maxLines = 1)
        }
    }
}

/** iOS `CountryFlagIcon`: bundled aircraft-with-flag picture named by the API country name, or the default aircraft flag icon. */
@Composable
private fun FlagCell(c: CountryOfOperation, onClick: () -> Unit) {
    val name = c.name ?: stringResource(R.string.ds_unknown)
    val flagDescription = stringResource(R.string.details_flag_cd, name, c.aircraftCount)
    Box(
        Modifier.defaultMinSize(TGSize.tapMin, TGSize.tapMin).size(TGDetailSize.flag).clip(TGDetailSize.cellRadius)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.details_show_country_name), onClick = onClick).semantics { contentDescription = flagDescription },
        contentAlignment = Alignment.Center,
    ) {
        // iOS: UIImage(named: country.name) (bundled aircraft-with-flag picture) else aircraft_flag_icon; never the API flag_icon.
        val painter = painterResource(FlagAircraftImages.forCountry(c.name) ?: R.drawable.aircraft_flag_icon)
        // iOS .shadow(black 0.3, radius 5, x 2, y 2): a blurred, offset, black copy of the picture behind it (blur needs Android 12+; older versions show a hard copy).
        Image(
            painter, contentDescription = null, contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.Black, BlendMode.SrcIn),
            modifier = Modifier.requiredSize(TGDetailSize.flagImage)
                .offset(TGDetailSize.flagShadowOffset, TGDetailSize.flagShadowOffset)
                .alpha(TGDetailSize.flagShadowAlpha)
                .blur(TGDetailSize.flagShadowRadius / 2, BlurredEdgeTreatment.Unbounded),
        )
        Image(painter, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.requiredSize(TGDetailSize.flagImage))
    }
}

// -------------------------------------------------------- manufacturer

/** iOS `ManufacturerInfoView`: round logo and the name. */
@Composable
private fun ManufacturerRow(m: ManufacturerBrief, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = TGSize.tapMin).clip(TGRadius.card).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
    ) {
        Box(Modifier.padding(start = TGSpace.s).size(TGDetailSize.logo + TGDetailSize.logoPadding * 2).background(TG.colors.card, CircleShape).padding(TGDetailSize.logoPadding), contentAlignment = Alignment.Center) {
            if (m.logo.isNullOrBlank()) {
                Image(painterResource(R.drawable.fan1), contentDescription = null, modifier = Modifier.fillMaxSize())
            } else {
                TGImage(m.logo, contentDescription = stringResource(R.string.details_logo_cd, m.name), contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            }
        }
        Text(m.name, style = TGText.sailecBold(17), color = TG.colors.textPrimary, modifier = Modifier.weight(1f))
    }
}

// ------------------------------------------------------ variants etc.

@Composable
private fun AircraftRail(title: String, owner: AircraftDetail, items: List<app.topgun.core.model.Aircraft>, user: UserState, a: DetailActions) {
    Column(verticalArrangement = Arrangement.spacedBy(TGSpace.m)) {
        DetailHeading(title, R.drawable.card, Modifier.fillMaxWidth())
        items.filter { it.id != owner.id }.forEach { item ->
            AircraftListCard(item, isLocked = item.isVip && !user.isPremium, onClick = { a.onOpenAircraft(item.id) })
        }
    }
}

// -------------------------------------------------------------- rating

/** iOS `ratingSection`: five stars on a dark card. Signed-out taps are handled by the caller. */
@Composable
private fun RatingSection(ui: DetailsState, onRate: (Int) -> Unit) {
    Column(Modifier.padding(horizontal = TGDetailSpace.ten), verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
        Text(stringResource(R.string.details_rate_this), style = TGText.sailecBold(18), color = TG.colors.textPrimary)
        Column(
            Modifier.fillMaxWidth().background(TGBrand.darkBack.copy(alpha = 0.92f), TGRadius.card).padding(vertical = TGSpace.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TGSpace.m),
        ) {
            DetailStars(ui.userRating, TGDetailSize.ratingStar, onRate = onRate, enabled = !ui.isRating, emptyTint = TGDetailColors.onPhoto.copy(alpha = 0.4f))
            if (ui.isRating) {
                CircularProgressIndicator(color = TGDetailColors.onPhoto, modifier = Modifier.size(TGSpace.xl))
            } else if (ui.userRating > 0) {
                Text(stringResource(R.string.details_rating_submitted), style = TGText.sailecRegular(13), color = TGDetailColors.onPhoto.copy(alpha = 0.8f))
            }
        }
    }
}

