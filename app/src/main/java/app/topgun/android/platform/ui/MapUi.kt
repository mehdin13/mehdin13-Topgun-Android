// ANDROID-ONLY
package app.topgun.android.platform.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import app.topgun.android.R
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGGeoColor
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.TGImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
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
 * - [Globe]: a spinning globe drawn with a Compose Canvas, no 3D engine. The earth texture (`earth_texture.jpg`,
 *   same image the iOS SceneKit globe uses) is sampled through an orthographic projection into a small bitmap
 *   (yaw only, 120 s per turn like iOS; drag to turn it). Pins are real composables laid over the sphere, so they
 *   show the country flag and are tappable; pins on the far side are moved off-screen.
 */
internal object MapUi {

    // ------------------------------------------------------------------ flat map

    @Composable
    fun World(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) {
        // iOS MapView: centre 6.6N 16.4E, span 70 degrees (about zoom 2.5). A single pin zooms in on it.
        val start = if (pins.size == 1) {
            CameraPosition.fromLatLngZoom(LatLng(pins[0].latitude, pins[0].longitude), SINGLE_PIN_ZOOM)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), DEFAULT_ZOOM)
        }
        val camera = rememberCameraPositionState { position = start }
        GoogleMap(
            modifier = modifier,
            cameraPositionState = camera,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        ) {
            pins.forEach { pin ->
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
    }

    // ------------------------------------------------------------------ globe

    @Composable
    fun Globe(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) {
        val texture = rememberEarthTexture()
        val grid = remember { GlobeGrid() }
        val bitmap = remember { Bitmap.createBitmap(GRID, GRID, Bitmap.Config.ARGB_8888) }
        val out = remember { IntArray(GRID * GRID) }

        var yaw by remember { mutableFloatStateOf(pins.firstOrNull()?.let { Math.toRadians(it.longitude).toFloat() } ?: 0f) }
        var dragging by remember { mutableStateOf(false) }

        // Auto-spin while the finger is up. withFrameNanos keeps the speed independent of the frame rate.
        LaunchedEffect(dragging) {
            if (!dragging) {
                var last = androidx.compose.runtime.withFrameNanos { it }
                while (true) {
                    val now = androidx.compose.runtime.withFrameNanos { it }
                    yaw += (now - last) / NANOS_PER_SECOND * SPIN_RAD_PER_SECOND
                    last = now
                }
            }
        }

        BoxWithConstraints(modifier.background(TGGeoColor.globeSpace), contentAlignment = Alignment.Center) {
            val density = LocalDensity.current
            val sidePx = with(density) { minOf(maxWidth, maxHeight).toPx() }
            val diameter = sidePx * GLOBE_FILL
            val radius = diameter / 2f
            val diameterDp = with(density) { diameter.toDp() }
            val radiusState = rememberUpdatedState(radius)
            val pinPx = with(density) { TGGeo.pin.toPx() }

            Box(
                Modifier.size(diameterDp).pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                    ) { change, drag ->
                        change.consume()
                        yaw -= drag.x / radiusState.value
                    }
                },
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val tex = texture
                    if (tex != null) {
                        render(grid, tex, yaw, out)
                        bitmap.setPixels(out, 0, GRID, 0, 0, GRID, GRID)
                        drawImage(
                            bitmap.asImageBitmap(),
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                            filterQuality = FilterQuality.Medium,
                        )
                    } else {
                        drawCircle(TGBrand.blue.copy(alpha = TGGeo.scrim), radius = size.width / 2f)
                    }
                    // Thin atmosphere ring.
                    drawCircle(
                        Color.White.copy(alpha = TGGeo.glass), radius = size.width / 2f,
                        center = Offset(size.width / 2f, size.height / 2f), style = Stroke(width = 2f),
                    )
                }

                pins.forEach { pin ->
                    key(pin.id) {
                        Box(
                            Modifier
                                .offset { pinOffset(pin, yaw, radiusState.value, pinPx) }
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

    /** Top-left corner (px, inside the globe box) of [pin]; far-side pins go off-screen so they cannot be tapped. */
    private fun pinOffset(pin: MapPin, yaw: Float, radius: Float, pinPx: Float): IntOffset {
        val lat = Math.toRadians(pin.latitude)
        val lon = Math.toRadians(pin.longitude) - yaw
        val z = cos(lat) * cos(lon)
        if (z <= HIDE_BELOW_Z) return IntOffset(OFF_SCREEN, OFF_SCREEN)
        val x = radius + radius * (cos(lat) * sin(lon)).toFloat() - pinPx / 2f
        val y = radius - radius * sin(lat).toFloat() - pinPx / 2f
        return IntOffset(x.roundToInt(), y.roundToInt())
    }

    /** For every pixel of the small globe bitmap: texture row, longitude relative to the view centre, and shade. */
    private class GlobeGrid {
        val lonRel = FloatArray(GRID * GRID)
        val row = FloatArray(GRID * GRID)   // 0..1 from the north pole
        val shade = IntArray(GRID * GRID)    // 0..256, -1 = outside the sphere

        init {
            for (j in 0 until GRID) for (i in 0 until GRID) {
                val k = j * GRID + i
                val nx = (i + 0.5f) / GRID * 2f - 1f
                val ny = 1f - (j + 0.5f) / GRID * 2f
                val r2 = nx * nx + ny * ny
                if (r2 > 1f) {
                    shade[k] = -1
                } else {
                    val z = sqrt(1f - r2)
                    lonRel[k] = atan2(nx, z)
                    row[k] = (0.5f - asin(ny) / PI.toFloat())
                    shade[k] = ((AMBIENT + (1f - AMBIENT) * z) * 256f).toInt()
                }
            }
        }
    }

    private class EarthTexture(val width: Int, val height: Int, val pixels: IntArray)

    /** Renders the sphere centred on longitude [yaw] into [out] (ARGB). */
    private fun render(grid: GlobeGrid, tex: EarthTexture, yaw: Float, out: IntArray) {
        val twoPi = (2.0 * PI).toFloat()
        for (k in out.indices) {
            val sh = grid.shade[k]
            if (sh < 0) {
                out[k] = 0
                continue
            }
            var u = (grid.lonRel[k] + yaw) / twoPi + 0.5f
            u -= floor(u)
            val col = min((u * tex.width).toInt(), tex.width - 1)
            val rowIndex = min((grid.row[k] * tex.height).toInt(), tex.height - 1)
            val c = tex.pixels[rowIndex * tex.width + col]
            val r = (((c shr 16) and 0xFF) * sh) shr 8
            val g = (((c shr 8) and 0xFF) * sh) shr 8
            val b = ((c and 0xFF) * sh) shr 8
            out[k] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
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

    // Globe tuning: iOS rotates a full turn in 120 s.
    private const val GRID = 256
    private const val MAX_TEXTURE_WIDTH = 1024
    private const val GLOBE_FILL = 0.9f
    private const val AMBIENT = 0.45f
    private const val NANOS_PER_SECOND = 1_000_000_000f
    private val SPIN_RAD_PER_SECOND = (2.0 * PI / 120.0).toFloat()
    private const val HIDE_BELOW_Z = 0.05
    private const val OFF_SCREEN = -100_000
}
