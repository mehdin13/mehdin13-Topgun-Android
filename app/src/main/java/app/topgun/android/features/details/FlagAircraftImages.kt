package app.topgun.android.features.details

import app.topgun.android.R

/**
 * iOS `Assets.xcassets/FlagAircraft/<API country name>`: aircraft-with-flag pictures looked up by the exact API country
 * name (`UIImage(named: country.name)`). Generated from the iOS asset catalog; lookup ignores case and surrounding blanks.
 */
object FlagAircraftImages {
    private val byName: Map<String, Int> = mapOf(
        "algeria" to R.drawable.flagaircraft_algeria,
        "argentina" to R.drawable.flagaircraft_argentina,
        "australia" to R.drawable.flagaircraft_australia,
        "belgium" to R.drawable.flagaircraft_belgium,
        "brazil" to R.drawable.flagaircraft_brazil,
        "canada" to R.drawable.flagaircraft_canada,
        "chile" to R.drawable.flagaircraft_chile,
        "china" to R.drawable.flagaircraft_china,
        "croatia" to R.drawable.flagaircraft_croatia,
        "denmark" to R.drawable.flagaircraft_denmark,
        "egypt" to R.drawable.flagaircraft_egypt,
        "finland" to R.drawable.flagaircraft_finland,
        "france" to R.drawable.flagaircraft_france,
        "gabon" to R.drawable.flagaircraft_gabon,
        "germany" to R.drawable.flagaircraft_germany,
        "greece" to R.drawable.flagaircraft_greece,
        "india" to R.drawable.flagaircraft_india,
        "indonesia" to R.drawable.flagaircraft_indonesia,
        "iran, islamic republic of" to R.drawable.flagaircraft_iran_islamic_republic_of,
        "italy" to R.drawable.flagaircraft_italy,
        "japan" to R.drawable.flagaircraft_japan,
        "jordan" to R.drawable.flagaircraft_jordan,
        "kazakhstan" to R.drawable.flagaircraft_kazakhstan,
        "korea, democratic people's republic of" to R.drawable.flagaircraft_korea_democratic_people_s_republic_of,
        "korea, republic of" to R.drawable.flagaircraft_korea_republic_of,
        "malaysia" to R.drawable.flagaircraft_malaysia,
        "nigeria" to R.drawable.flagaircraft_nigeria,
        "norway" to R.drawable.flagaircraft_norway,
        "pakistan" to R.drawable.flagaircraft_pakistan,
        "philippines" to R.drawable.flagaircraft_philippines,
        "poland" to R.drawable.flagaircraft_poland,
        "portugal" to R.drawable.flagaircraft_portugal,
        "qatar" to R.drawable.flagaircraft_qatar,
        "romania" to R.drawable.flagaircraft_romania,
        "russia" to R.drawable.flagaircraft_russia,
        "saudi arabia" to R.drawable.flagaircraft_saudi_arabia,
        "singapore" to R.drawable.flagaircraft_singapore,
        "south africa" to R.drawable.flagaircraft_south_africa,
        "spain" to R.drawable.flagaircraft_spain,
        "sweden" to R.drawable.flagaircraft_sweden,
        "switzerland" to R.drawable.flagaircraft_switzerland,
        "taiwan, province of china" to R.drawable.flagaircraft_taiwan_province_of_china,
        "thailand" to R.drawable.flagaircraft_thailand,
        "turkey" to R.drawable.flagaircraft_turkey,
        "ukraine" to R.drawable.flagaircraft_ukraine,
        "united arab emirates" to R.drawable.flagaircraft_united_arab_emirates,
        "united kingdom" to R.drawable.flagaircraft_united_kingdom,
        "united states" to R.drawable.flagaircraft_united_states,
        "uzbekistan" to R.drawable.flagaircraft_uzbekistan,
        "venezuela" to R.drawable.flagaircraft_venezuela,
        "vietnam" to R.drawable.flagaircraft_vietnam,
        "belarus" to R.drawable.flagaircraft_belarus,
        "iraq" to R.drawable.flagaircraft_iraq,
        "israel" to R.drawable.flagaircraft_israel,
        "kenya" to R.drawable.flagaircraft_kenya,
        "kuwait" to R.drawable.flagaircraft_kuwait,
        "oman" to R.drawable.flagaircraft_oman,
    )

    /** Drawable id for the API country name, or null (callers then show `aircraft_flag_icon`). */
    fun forCountry(name: String?): Int? = name?.trim()?.lowercase()?.let { byName[it] }
}
