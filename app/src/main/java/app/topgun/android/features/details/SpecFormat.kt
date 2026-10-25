package app.topgun.android.features.details

import app.topgun.android.R
import java.util.Locale

enum class UnitSystem(val labelRes: Int) {
    Metric(R.string.details_units_metric),
    Imperial(R.string.details_units_imperial),
}

/** Localized unit suffixes and the "not available" text, read from string resources by the composable that owns the page. */
class SpecUnits(
    val km: String, val mi: String, val kmh: String, val mph: String,
    val kg: String, val lbs: String, val liter: String, val gal: String,
    val m: String, val ft: String, val na: String,
)

/** The unit conversions of iOS `SpecialDetailView` (values arrive as strings; unparsable values show the "N/A" text). */
class SpecFormat(private val units: UnitSystem, private val u: SpecUnits) {
    private val imperial get() = units == UnitSystem.Imperial

    val na: String get() = u.na

    fun km(v: String?): String = number(v)?.let { if (imperial) "${(it * 0.621371).toInt()} ${u.mi}" else "${it.toInt()} ${u.km}" } ?: u.na
    fun kph(v: String?): String = number(v)?.let { if (imperial) "${(it * 0.621371).toInt()} ${u.mph}" else "${it.toInt()} ${u.kmh}" } ?: u.na
    fun kg(v: String?): String = number(v)?.let { if (imperial) "${(it * 2.20462).toInt()} ${u.lbs}" else "${it.toInt()} ${u.kg}" } ?: u.na
    fun liters(v: String?): String = number(v)?.let { if (imperial) "${(it * 0.264172).toInt()} ${u.gal}" else "${it.toInt()} ${u.liter}" } ?: u.na
    fun meters(v: String?): String = number(v)?.let {
        if (imperial) String.format(Locale.US, "%.1f ", it * 3.28084) + u.ft else String.format(Locale.US, "%.1f ", it) + u.m
    } ?: u.na

    private fun number(v: String?): Double? = v?.trim()?.toDoubleOrNull()
}
