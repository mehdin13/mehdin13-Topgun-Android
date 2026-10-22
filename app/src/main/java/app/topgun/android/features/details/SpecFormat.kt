package app.topgun.android.features.details

import java.util.Locale

enum class UnitSystem(val label: String) { Metric("Metric"), Imperial("Imperial") }

/** The unit conversions of iOS `SpecialDetailView` (values arrive as strings; unparsable values show "N/A"). */
class SpecFormat(private val units: UnitSystem) {
    private val imperial get() = units == UnitSystem.Imperial

    fun km(v: String?): String = number(v)?.let { if (imperial) "${(it * 0.621371).toInt()} mi" else "${it.toInt()} km" } ?: NA
    fun kph(v: String?): String = number(v)?.let { if (imperial) "${(it * 0.621371).toInt()} mph" else "${it.toInt()} km/h" } ?: NA
    fun kg(v: String?): String = number(v)?.let { if (imperial) "${(it * 2.20462).toInt()} lbs" else "${it.toInt()} kg" } ?: NA
    fun liters(v: String?): String = number(v)?.let { if (imperial) "${(it * 0.264172).toInt()} gal" else "${it.toInt()} L" } ?: NA
    fun meters(v: String?): String = number(v)?.let {
        if (imperial) String.format(Locale.US, "%.1f ft", it * 3.28084) else String.format(Locale.US, "%.1f m", it)
    } ?: NA

    private fun number(v: String?): Double? = v?.trim()?.toDoubleOrNull()

    companion object { const val NA = "N/A" }
}
