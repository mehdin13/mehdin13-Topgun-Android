package app.topgun.core.model

/**
 * Filter sheet state, sent to `aircraft/aircraft_filters/` as form fields (see [toFormFields]).
 * Mirrors the iOS `AircraftFilterRequest`.
 *
 * The backend only understands: category, manufacturer, countries_of_operation, generation, role, status,
 * primary_user, hardpoints, crew_size, price_range, speed, radar_range, first_flight, stealth_capabilities
 * and vip_status ("vip" / "free"; iOS sends "true", which the backend ignores). `country_of_origin`, `introduction_year`, `electronic_warfare` and `range`
 * (which iOS also sends) are ignored by it today (see docs/BACKEND_GAPS.md).
 */
data class AircraftFilter(
    val category: String? = null,
    val countryOfOrigin: String? = null,
    val generations: List<String> = emptyList(),
    val roles: List<String> = emptyList(),
    val stealthCapabilities: List<String> = emptyList(),
    val status: String? = null,
    /** Maximum price in millions of USD. 100 means "no limit" (the slider's default). */
    val priceRange: Double? = null,
    val speed: String? = null,
    val primaryUsers: List<String> = emptyList(),
    val hardpoints: String? = null,
    /** Maximum range in km. 1000 means "no limit" (the slider's default). */
    val range: Double? = null,
    val manufacturer: String? = null,
    val introYear: Double? = null,
    val countriesOfOperation: List<String> = emptyList(),
    val crewSize: Int? = null,
    val hasElectronicWarfare: Boolean? = null,
    val isVip: Boolean? = null,
) {
    val isEmpty: Boolean get() = toFormFields().isEmpty()

    /** Non-default values only, in the exact field names and formats the iOS app sends. */
    fun toFormFields(): Map<String, String> = buildMap {
        category?.takeIf { it.isNotEmpty() }?.let { put("category", it) }
        countryOfOrigin?.takeIf { it.isNotEmpty() }?.let { put("country_of_origin", it) }
        if (generations.isNotEmpty()) put("generation", generations.joinToString(","))
        if (roles.isNotEmpty()) put("role", roles.joinToString(","))
        if (stealthCapabilities.isNotEmpty()) put("stealth_capabilities", stealthCapabilities.joinToString(","))
        status?.takeIf { it.isNotEmpty() }?.let { put("status", it) }
        priceRange?.takeIf { it != 100.0 }?.let { put("price_range", "0,${it.toInt()}") }
        speed?.takeIf { it.isNotEmpty() }?.let { put("speed", it) }
        if (primaryUsers.isNotEmpty()) put("primary_user", primaryUsers.joinToString(","))
        hardpoints?.takeIf { it.isNotEmpty() }?.let { put("hardpoints", it) }
        range?.takeIf { it != 1000.0 }?.let { put("range", "0,${it.toInt()}") }
        manufacturer?.takeIf { it.isNotEmpty() }?.let { put("manufacturer", it) }
        introYear?.takeIf { it >= 1950 }?.let { put("introduction_year", it.toInt().toString()) }
        if (countriesOfOperation.isNotEmpty()) put("countries_of_operation", countriesOfOperation.joinToString(","))
        crewSize?.takeIf { it > 1 }?.let { put("crew_size", it.toString()) }
        if (hasElectronicWarfare == true) put("electronic_warfare", "true")
        if (isVip == true) put("vip_status", "vip")
    }
}
