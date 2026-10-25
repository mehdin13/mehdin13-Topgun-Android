package app.topgun.android.features.countries

import androidx.compose.ui.res.stringResource
import app.topgun.android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import app.topgun.android.designsystem.TGGeo
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.platform.ui.LocalPlatformUi
import app.topgun.android.platform.ui.MapPin
import app.topgun.core.model.Country

/** Pin for a country with known coordinates (iOS `CountryOfOperationModel` -> map annotation). */
/** [aircraftLabel] is the translated "Aircraft:" caption shown before the count. */
fun Country.toMapPin(aircraftLabel: String): MapPin? {
    val la = latitude ?: return null
    val lo = longitude ?: return null
    return MapPin(id, name, "$aircraftLabel $aircraftCount", la, lo, flagUrl = flagIcon ?: icon)
}

/**
 * Flat map or globe of [countries] with the glass callout of the tapped pin ("More Info" opens the country).
 * iOS: MapView (flat map / globe toggle), GlobeView, GlassmorphicCalloutView.
 */
@Composable
fun CountryMapPanel(countries: List<Country>, globe: Boolean, onOpenCountry: (Int) -> Unit, modifier: Modifier = Modifier) {
    val ui = LocalPlatformUi.current
    val aircraftLabel = stringResource(R.string.countries_pin_aircraft)
    val pins = remember(countries, aircraftLabel) { countries.mapNotNull { it.toMapPin(aircraftLabel) } }
    var selected by remember { mutableStateOf<MapPin?>(null) }
    Box(modifier) {
        if (globe) ui.Globe(pins, Modifier.fillMaxSize()) { selected = it }
        else ui.WorldMap(pins, Modifier.fillMaxSize()) { selected = it }
        selected?.let { pin ->
            GlassCallout(
                title = pin.title, subtitle = pin.subtitle.orEmpty(),
                onMoreInfo = { onOpenCountry(pin.id) }, onDismiss = { selected = null },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = TGSpace.xxl),
            )
        }
    }
}

/** iOS GlassmorphicCalloutView: translucent rounded panel with title, subtitle and a "More Info" capsule. */
@Composable
fun GlassCallout(title: String, subtitle: String, onMoreInfo: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.widthIn(max = TGGeo.calloutWidth).clip(TGGeo.shape20)
            .background(Color.Black.copy(alpha = TGGeo.scrim))
            .border(TGSpace.xs / 4, Color.White.copy(alpha = TGGeo.scrimLight), TGGeo.shape20)
            .clickable(onClick = onDismiss)
            .padding(TGSpace.l + TGSpace.xs),
        verticalArrangement = Arrangement.spacedBy(TGSpace.m),
    ) {
        Text(title, style = TGText.sailecBold(18), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, style = TGText.sailecMedium(14), color = Color.White.copy(alpha = 0.9f), maxLines = 2)
        Row(
            Modifier.align(Alignment.End).heightIn(min = TGSize.tapMin).clip(CircleShape)
                .background(Color.White.copy(alpha = TGGeo.glass))
                .clickable(role = Role.Button, onClick = onMoreInfo)
                .padding(horizontal = TGSpace.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
        ) {
            Text(stringResource(R.string.countries_more_info), style = TGText.sailecBold(14), color = Color.White)
            Icon(Icons.Filled.ArrowCircleRight, contentDescription = null, tint = Color.White)
        }
    }
}
