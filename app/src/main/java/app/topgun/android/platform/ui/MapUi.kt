// ANDROID-ONLY
package app.topgun.android.platform.ui

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import app.topgun.android.R
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGGeoColor
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.MapNoticeBox
import app.topgun.android.designsystem.components.TGImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Map and globe behind [PlatformUi.WorldMap] / [PlatformUi.Globe].
 *
 * - [World]: Google Maps (maps-compose) with one default marker per pin. Marker taps are reported through
 *   `onPinClick` and the info window is suppressed because the screen shows its own callout (iOS GlassmorphicCalloutView).
 *   If the manifest has no Maps key, or the map has not reported `onMapLoaded` after [MAP_LOAD_TIMEOUT_MS], a message
 *   explains what to check instead of showing a blank grid.
 * - [Globe]: a spinning globe drawn with a Compose Canvas, no 3D engine and no Google Maps. The earth texture
 *   (`earth_texture.jpg`, same image the iOS SceneKit globe uses) is sampled through an orthographic projection into
 *   a small bitmap on a background thread. Gestures: drag turns it (yaw) and tilts it (pitch), pinch zooms,
 *   double tap resets. Pins are real composables laid over the sphere, so they show the country flag, follow the
 *   same projection while zoomed and are tappable; pins on the far side are moved off-screen.
 */
internal object MapUi {

    // ------------------------------------------------------------------ flat map

    @Composable
    fun World(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) {
        val context = LocalContext.current
        val keyMissing = remember { mapsKeyMissing(context) }
        if (keyMissing) {
            MapNoticeBox(MESSAGE_KEY_MISSING, modifier)
            return
        }
        val valid = remember(pins) { pins.filter { it.hasValidCoordinates() }.distinctBy { it.id } }
        // iOS MapView: centre 6.6N 16.4E, span 70 degrees (about zoom 2.5). A single pin zooms in on it.
        val start = if (valid.size == 1) {
            CameraPosition.fromLatLngZoom(LatLng(valid[0].latitude, valid[0].longitude), SINGLE_PIN_ZOOM)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), DEFAULT_ZOOM)
        }
        val camera = rememberCameraPositionState { position = start }
        var loaded by remember { mutableStateOf(false) }
        var timedOut by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(MAP_LOAD_TIMEOUT_MS)
            if (!loaded) timedOut = true
        }
        Box(modifier) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false, mapToolbarEnabled = false,
                    zoomGesturesEnabled = true, scrollGesturesEnabled = true, rotationGesturesEnabled = true,
                ),
                onMapLoaded = {
                    loaded = true
                    timedOut = false
                },
            ) {
                valid.forEach { pin ->
                    key(pin.id) {
                        Marker(
                            state = rememberMarkerState(key = pin.id.toString(), position = LatLng(pin.latitude, pin.longitude)),
                            title = pin.title,
                            snippet = pin.subtitle,
                            onClick = {
                                onPinClick(pin)
                                true
                            },
                        )
                    }
                }
            }
            if (!loaded) {
                if (timedOut) {
                    MapNoticeBox(MESSAGE_LOAD_FAILED, Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TG.colors.accent)
                    }
                }
            }
        }
    }

    /** True when the manifest `com.google.android.geo.API_KEY` meta-data is missing or blank (no `topgun.mapsApiKey`). */
    private fun mapsKeyMissing(context: Context): Boolean = try {
        val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        info.metaData?.getString(MAPS_KEY_META).isNullOrBlank()
    } catch (e: Exception) {
        false
    }

    private fun MapPin.hasValidCoordinates(): Boolean =
        latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0

    // ------------------------------------------------------------------ globe

    /** The view the renderer needs; equal values are not re-rendered. */
    private data class GlobeView(val yaw: Float, val pitch: Float, val zoom: Float, val interacting: Boolean)

    private class GlobeBuffers(val n: Int) {
        val bitmap: Bitmap = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        val out = IntArray(n * n)
        val image: ImageBitmap = bitmap.asImageBitmap()
    }

    @Composable
    fun Globe(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) {
        val valid = remember(pins) { pins.filter { it.hasValidCoordinates() }.distinctBy { it.id } }
        val texture = rememberEarthTexture()
        val sharp = remember { GlobeBuffers(GRID_IDLE) }
        val coarse = remember { GlobeBuffers(GRID_MOVING) }

        var yaw by remember { mutableFloatStateOf(valid.firstOrNull()?.let { Math.toRadians(it.longitude).toFloat() } ?: 0f) }
        var pitch by remember { mutableFloatStateOf(0f) }
        var zoom by remember { mutableFloatStateOf(1f) }
        var interacting by remember { mutableStateOf(false) }
        var shown by remember { mutableStateOf<ImageBitmap?>(null) }
        var tick by remember { mutableIntStateOf(0) }

        // Auto-spin while no finger is down. withFrameNanos keeps the speed independent of the frame rate.
        LaunchedEffect(interacting) {
            if (!interacting) {
                var last = androidx.compose.runtime.withFrameNanos { it }
                while (true) {
                    val now = androidx.compose.runtime.withFrameNanos { it }
                    yaw += (now - last) / NANOS_PER_SECOND * SPIN_RAD_PER_SECOND
                    last = now
                }
            }
        }

        // Render off the main thread; fewer pixels while a finger is moving the globe.
        LaunchedEffect(texture) {
            val tex = texture ?: return@LaunchedEffect
            snapshotFlow { GlobeView(yaw, pitch, zoom, interacting) }.collectLatest { view ->
                val buf = if (view.interacting) coarse else sharp
                withContext(Dispatchers.Default) { render(buf, tex, view) }
                buf.bitmap.setPixels(buf.out, 0, buf.n, 0, 0, buf.n, buf.n)
                shown = buf.image
                tick++
            }
        }

        BoxWithConstraints(modifier.background(TGGeoColor.globeSpace), contentAlignment = Alignment.Center) {
            val density = LocalDensity.current
            val sidePx = with(density) { minOf(maxWidth, maxHeight).toPx() }
            val sideDp = with(density) { sidePx.toDp() }
            val pinPx = with(density) { TGGeo.pin.toPx() }

            Box(
                Modifier
                    .size(sideDp)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            zoom = 1f
                            pitch = 0f
                        })
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            interacting = true
                            do {
                                val event = awaitPointerEvent()
                                val zoomChange = event.calculateZoom()
                                val pan = event.calculatePan()
                                if (zoomChange != 1f || pan != Offset.Zero) {
                                    zoom = (zoom * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                    val radius = size.width * GLOBE_FILL / 2f * zoom
                                    if (radius > 0f) {
                                        yaw -= pan.x / radius
                                        pitch = (pitch + pan.y / radius).coerceIn(-MAX_PITCH, MAX_PITCH)
                                    }
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                            interacting = false
                        }
                    },
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    @Suppress("UNUSED_VARIABLE") val redraw = tick
                    val image = shown
                    if (image != null) {
                        drawImage(
                            image,
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                            filterQuality = FilterQuality.Medium,
                        )
                    } else {
                        drawCircle(TGBrand.blue.copy(alpha = TGGeo.scrim), radius = size.width * GLOBE_FILL / 2f * zoom)
                    }
                    // Thin atmosphere ring.
                    drawCircle(
                        Color.White.copy(alpha = TGGeo.glass), radius = size.width * GLOBE_FILL / 2f * zoom,
                        center = Offset(size.width / 2f, size.height / 2f), style = Stroke(width = 2f),
                    )
                }

                valid.forEach { pin ->
                    key(pin.id) {
                        Box(
                            Modifier
                                .offset { pinOffset(pin, yaw, pitch, sidePx, zoom, pinPx) }
                                .size(TGGeo.pin)
                                .clip(CircleShape)
                                .background(TGBrand.blue)
                                .border(TGGeo.coverLogoBorder, Color.White, CircleShape)
                                .clickable(role = Role.Button, onClickLabel = "Show ${pin.title}") { onPinClick(pin) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (pin.flagUrl.isNullOrBlank()) {
                                Text(pin.title.take(2).uppercase(), style = TGText.sailecBold(10), color = Color.White)
                            } else {
                                TGImage(pin.flagUrl, "${pin.title} flag", Modifier.fillMaxSize(), ContentScale.Crop)
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ globe maths

    /**
     * Top-left corner (px, inside the square globe box of side [side]) of [pin]. Same projection as [render]:
     * rotate by yaw around the polar axis, then tilt by pitch around the horizontal axis. Pins on the far side go
     * off-screen so they cannot be tapped.
     */
    private fun pinOffset(pin: MapPin, yaw: Float, pitch: Float, side: Float, zoom: Float, pinPx: Float): IntOffset {
        val lat = Math.toRadians(pin.latitude)
        val d = Math.toRadians(pin.longitude) - yaw
        val x = cos(lat) * sin(d)
        val y = sin(lat)
        val z = cos(lat) * cos(d)
        val cp = cos(pitch.toDouble())
        val sp = sin(pitch.toDouble())
        val y2 = y * cp - z * sp
        val z2 = y * sp + z * cp
        if (z2 <= HIDE_BELOW_Z) return IntOffset(OFF_SCREEN, OFF_SCREEN)
        val radius = side * GLOBE_FILL / 2f * zoom
        val px = side / 2f + radius * x.toFloat() - pinPx / 2f
        val py = side / 2f - radius * y2.toFloat() - pinPx / 2f
        return IntOffset(px.roundToInt(), py.roundToInt())
    }

    private class EarthTexture(val width: Int, val height: Int, val pixels: IntArray)

    /** Renders the sphere for [view] into [buf] (ARGB, square viewport, inverse orthographic projection). */
    private fun render(buf: GlobeBuffers, tex: EarthTexture, view: GlobeView) {
        val n = buf.n
        val out = buf.out
        val twoPi = (2.0 * Math.PI).toFloat()
        val piF = Math.PI.toFloat()
        val radiusFrac = GLOBE_FILL / 2f * view.zoom
        val cp = cos(view.pitch)
        val sp = sin(view.pitch)
        for (j in 0 until n) {
            val ny = (0.5f - (j + 0.5f) / n) / radiusFrac
            for (i in 0 until n) {
                val k = j * n + i
                val nx = ((i + 0.5f) / n - 0.5f) / radiusFrac
                val r2 = nx * nx + ny * ny
                if (r2 > 1f) {
                    out[k] = 0
                    continue
                }
                val z = sqrt(1f - r2)
                // Undo the pitch tilt to get the point in globe space.
                val y = (ny * cp + z * sp).coerceIn(-1f, 1f)
                val z0 = -ny * sp + z * cp
                val lon = atan2(nx, z0) + view.yaw
                val lat = asin(y)
                var u = lon / twoPi + 0.5f
                u -= floor(u)
                val col = min((u * tex.width).toInt(), tex.width - 1)
                val row = min(((0.5f - lat / piF) * tex.height).toInt().coerceAtLeast(0), tex.height - 1)
                val shade = ((AMBIENT + (1f - AMBIENT) * z) * 256f).toInt()
                val c = tex.pixels[row * tex.width + col]
                val r = (((c shr 16) and 0xFF) * shade) shr 8
                val g = (((c shr 8) and 0xFF) * shade) shr 8
                val b = ((c and 0xFF) * shade) shr 8
                out[k] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
    }

    @Composable
    private fun rememberEarthTexture(): EarthTexture? {
        val context = LocalContext.current
        var texture by remember { mutableStateOf<EarthTexture?>(null) }
        LaunchedEffect(Unit) { texture = withContext(Dispatchers.Default) { loadEarth(context) } }
        return texture
    }

    /** Decodes `earth_texture.jpg` down to at most [MAX_TEXTURE_WIDTH] px wide and keeps the pixels for sampling. */
    private fun loadEarth(context: Context): EarthTexture? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, R.drawable.earth_texture, bounds)
            var sample = 1
            while (bounds.outWidth / sample > MAX_TEXTURE_WIDTH) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.earth_texture, options) ?: return null
            val pixels = IntArray(bmp.width * bmp.height)
            bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
            val tex = EarthTexture(bmp.width, bmp.height, pixels)
            bmp.recycle()
            tex
        } catch (e: Exception) {
            null
        }
    }

    // Camera of the flat map (iOS MapView / InsetMapView regions).
    private const val DEFAULT_LAT = 6.6
    private const val DEFAULT_LNG = 16.4
    private const val DEFAULT_ZOOM = 2.5f
    private const val SINGLE_PIN_ZOOM = 4f
    private const val MAPS_KEY_META = "com.google.android.geo.API_KEY"
    private const val MAP_LOAD_TIMEOUT_MS = 12_000L
    private const val MESSAGE_KEY_MISSING =
        "Google Maps key missing. Set topgun.mapsApiKey (local.properties or the MAPS_API_KEY secret) and rebuild."
    private const val MESSAGE_LOAD_FAILED =
        "The map could not be loaded. Check the Maps API key restrictions (package app.topgun.android + signing SHA-1) " +
            "and that Maps SDK for Android is enabled."

    // Globe tuning: iOS rotates a full turn in 120 s. Android adds pinch zoom (1x-5x) and a +-80 degree tilt.
    private const val GRID_IDLE = 256
    private const val GRID_MOVING = 144
    private const val MAX_TEXTURE_WIDTH = 1024
    private const val GLOBE_FILL = 0.9f
    private const val AMBIENT = 0.45f
    private const val MIN_ZOOM = 1f
    private const val MAX_ZOOM = 5f
    private const val MAX_PITCH = 1.4f
    private const val NANOS_PER_SECOND = 1_000_000_000f
    private val SPIN_RAD_PER_SECOND = (2.0 * Math.PI / 120.0).toFloat()
    private const val HIDE_BELOW_Z = 0.05
    private const val OFF_SCREEN = -100_000
}
