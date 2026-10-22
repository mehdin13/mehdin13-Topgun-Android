package app.topgun.android.features.home

import androidx.lifecycle.ViewModel
import app.topgun.core.model.AircraftFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** One country row in the "Countries of Operation" section; [code] is the ISO-2 code of the flag SVG. */
data class FilterCountry(val name: String, val code: String)

/** Speed bucket (iOS `SpeedCategory`): ranges are in mph, the API gets km/h as "min,max". */
enum class SpeedCategory(val label: String, val minMph: Int, val maxMph: Int) {
    Subsonic("Subsonic", 0, 614),
    Transonic("Transonic", 614, 921),
    Supersonic("Supersonic", 921, 3836),
    Hypersonic("Hypersonic", 3836, 19030);

    val minKph: Int get() = (minMph * MPH_TO_KMH).toInt()
    val maxKph: Int get() = (maxMph * MPH_TO_KMH).toInt()
    /** `speed` form value: "min,max" in km/h. */
    val apiValue: String get() = "$minKph,$maxKph"

    private companion object { const val MPH_TO_KMH = 1.60934 }
}

/** The option lists of the iOS FilterView, in the same order. */
object FilterOptions {
    val aircraftTypes = listOf("Fighter", "Bomber", "Helicopters", "Spy", "Electronic Warfare", "Reconnaissance", "Tanker", "Transport", "Trainer", "AWACS", "UAVs")

    /** iOS list ("United State" is a typo there; the backend ignores `country_of_origin` anyway). Pairs of name and flag code. */
    val originCountries = listOf(
        FilterCountry("United States", "us"), FilterCountry("China", "cn"), FilterCountry("Russia", "ru"),
        FilterCountry("France", "fr"), FilterCountry("Sweden", "se"), FilterCountry("Italy", "it"),
        FilterCountry("United Kingdom", "gb"), FilterCountry("Japan", "jp"), FilterCountry("South Korea", "kr"),
        FilterCountry("Turkey", "tr"), FilterCountry("India", "in"),
    )
    val generations = listOf("6th", "5th", "4th", "4.5th", "3th", "2th")
    val roles = listOf(
        "Multirole", "Air Superiority", "Ground Attack", "Reconnaissance", "Transport", "Precision Strike",
        "Close Air Support (CAS)", "Stealth Operations", "Escort", "Electronic Warfare", "Search and Rescue", "Maritime Surveillance",
    )
    val statuses = listOf("Active", "Retired", "In Service", "Under Development", "Out of Service", "Prototype", "Experimental")
    val stealth = listOf("Full Stealth", "Reduced Stealth", "No Stealth", "Stealth Features")
    val speeds = SpeedCategory.entries
    val primaryUsers = listOf("US Air Force", "Royal Air Force", "NATO")
    val hardpoints = listOf("<5 Hardpoints", "5–10 Hardpoints", ">10 Hardpoints")
    val manufacturers = listOf(
        "Lockheed Martin", "Boeing", "Northrop", "Grumman", "McDonnell Douglas", "General Dynamics", "Sukhoi", "Mikoyan",
        "Tupolev", "Ilyushin", "Yakovlev", "Chengdu", "Shenyang", "Dassault", "BAE Systems", "Mitsubishi", "Kawasaki",
        "Airbus", "Hensoldt", "Panavia", "HAL", "Leonardo", "Saab",
    )

    /** iOS `all_countries_complete.json` (name and flag), duplicates removed. */
    val operatingCountries = listOf(
        FilterCountry("Afghanistan", "af"),
        FilterCountry("Albania", "al"),
        FilterCountry("Angola", "ao"),
        FilterCountry("Argentina", "ar"),
        FilterCountry("Australia", "au"),
        FilterCountry("Austria", "at"),
        FilterCountry("Bahrain", "bh"),
        FilterCountry("Bangladesh", "bd"),
        FilterCountry("Belize", "bz"),
        FilterCountry("Bolivia", "bo"),
        FilterCountry("Brazil", "br"),
        FilterCountry("Brunei", "bn"),
        FilterCountry("Bulgaria", "bg"),
        FilterCountry("Cambodia", "kh"),
        FilterCountry("Canada", "ca"),
        FilterCountry("Central African Republic", "cf"),
        FilterCountry("China", "cn"),
        FilterCountry("Colombia", "co"),
        FilterCountry("Comoros", "km"),
        FilterCountry("Congo (DRC)", "cd"),
        FilterCountry("Czechia", "cz"),
        FilterCountry("Denmark", "dk"),
        FilterCountry("Djibouti", "dj"),
        FilterCountry("Dominican Republic", "do"),
        FilterCountry("Egypt", "eg"),
        FilterCountry("El Salvador", "sv"),
        FilterCountry("Estonia", "ee"),
        FilterCountry("Finland", "fi"),
        FilterCountry("France", "fr"),
        FilterCountry("Georgia", "ge"),
        FilterCountry("Germany", "de"),
        FilterCountry("Greece", "gr"),
        FilterCountry("Hungary", "hu"),
        FilterCountry("India", "in"),
        FilterCountry("Indonesia", "id"),
        FilterCountry("Iran", "ir"),
        FilterCountry("Iraq", "iq"),
        FilterCountry("Ireland", "ie"),
        FilterCountry("Israel", "il"),
        FilterCountry("Italy", "it"),
        FilterCountry("Jamaica", "jm"),
        FilterCountry("Japan", "jp"),
        FilterCountry("North Korea", "kp"),
        FilterCountry("South Korea", "kr"),
        FilterCountry("Kuwait", "kw"),
        FilterCountry("Malaysia", "my"),
        FilterCountry("Moldova", "md"),
        FilterCountry("Monaco", "mc"),
        FilterCountry("Montenegro", "me"),
        FilterCountry("Morocco", "ma"),
        FilterCountry("Netherlands", "nl"),
        FilterCountry("New Zealand", "nz"),
        FilterCountry("Norway", "no"),
        FilterCountry("Oman", "om"),
        FilterCountry("Pakistan", "pk"),
        FilterCountry("Philippines", "ph"),
        FilterCountry("Poland", "pl"),
        FilterCountry("Portugal", "pt"),
        FilterCountry("Qatar", "qa"),
        FilterCountry("Romania", "ro"),
        FilterCountry("Russia", "ru"),
        FilterCountry("Saudi Arabia", "sa"),
        FilterCountry("Serbia", "rs"),
        FilterCountry("South Africa", "za"),
        FilterCountry("Spain", "es"),
        FilterCountry("Sri Lanka", "lk"),
        FilterCountry("Sweden", "se"),
        FilterCountry("Switzerland", "ch"),
        FilterCountry("Tanzania", "tz"),
        FilterCountry("Thailand", "th"),
        FilterCountry("Tunisia", "tn"),
        FilterCountry("Turkey", "tr"),
        FilterCountry("Ukraine", "ua"),
        FilterCountry("United Arab Emirates", "ae"),
        FilterCountry("United Kingdom", "gb"),
        FilterCountry("United States", "us"),
        FilterCountry("Venezuela", "ve"),
        FilterCountry("Vietnam", "vn")
    )

    // Slider ranges (iOS FilterView).
    const val PRICE_MIN = 10f
    const val PRICE_MAX = 300f
    const val PRICE_STEP = 10f
    const val RANGE_MAX = 5000f
    const val RANGE_STEP = 100f
    const val YEAR_MIN = 1950f
    const val YEAR_MAX = 2040f
    const val YEAR_STEP = 10f
}

/**
 * Everything the filter sheet edits. A slider at its far end means "no limit" and sends nothing; the iOS app
 * started its sliders at 100 / 1000 / 1000 (off their tracks), which this replaces with a clear "any" position.
 */
data class FilterState(
    val category: String? = null,
    val countryOfOrigin: String? = null,
    val generations: List<String> = emptyList(),
    val roles: List<String> = emptyList(),
    val stealth: List<String> = emptyList(),
    val status: String? = null,
    /** Maximum price in millions; [FilterOptions.PRICE_MAX] = any. */
    val priceMax: Float = FilterOptions.PRICE_MAX,
    val speed: SpeedCategory? = null,
    val primaryUsers: List<String> = emptyList(),
    val hardpoints: String? = null,
    /** Maximum range in km; [FilterOptions.RANGE_MAX] = any. */
    val rangeKm: Float = FilterOptions.RANGE_MAX,
    val electronicWarfare: Boolean = false,
    val manufacturer: String? = null,
    /** Introduction year from; [FilterOptions.YEAR_MIN] = any. */
    val introYear: Float = FilterOptions.YEAR_MIN,
    val vipOnly: Boolean = false,
    val crewSize: Int = 1,
    val countries: List<String> = emptyList(),
) {
    /**
     * The request for `api.filterAircraft`. VIP-only is dropped for non-Pro users (iOS `isPremium ? vipStatus : false`).
     * [AircraftFilter] treats exactly 100 (price) and 1000 (range) as "not set", so those two values are nudged by
     * half a unit; the form sends `toInt()`, which is unchanged.
     */
    fun toFilter(isPremium: Boolean): AircraftFilter = AircraftFilter(
        category = category,
        countryOfOrigin = countryOfOrigin,
        generations = generations,
        roles = roles,
        stealthCapabilities = stealth,
        status = status,
        priceRange = priceMax.takeIf { it < FilterOptions.PRICE_MAX }?.let { notDefault(it.toDouble(), 100.0) },
        speed = speed?.apiValue,
        primaryUsers = primaryUsers,
        hardpoints = hardpoints,
        range = rangeKm.takeIf { it < FilterOptions.RANGE_MAX }?.let { notDefault(it.toDouble(), 1000.0) },
        manufacturer = manufacturer,
        introYear = introYear.takeIf { it > FilterOptions.YEAR_MIN }?.toDouble(),
        countriesOfOperation = countries,
        crewSize = crewSize,
        hasElectronicWarfare = electronicWarfare,
        isVip = vipOnly && isPremium,
    )

    private fun notDefault(value: Double, default: Double) = if (value == default) value + 0.5 else value
}

/** Keeps the filter selections while the Home screen lives, so reopening the sheet shows what is applied. */
class FilterViewModel : ViewModel() {
    private val _state = MutableStateFlow(FilterState())
    val state: StateFlow<FilterState> = _state.asStateFlow()

    fun update(transform: FilterState.() -> FilterState) = _state.update { it.transform() }
    fun reset() { _state.value = FilterState() }
}
