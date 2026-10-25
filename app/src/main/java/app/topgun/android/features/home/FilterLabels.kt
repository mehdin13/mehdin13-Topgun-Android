package app.topgun.android.features.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.topgun.android.R

/**
 * Display names of the filter options. The English option text is also the value the API receives, so the lists in
 * [FilterOptions] keep it; only what the person reads is translated here. Unknown values (e.g. categories sent by
 * the backend) are shown as they are.
 */
object FilterLabels {
    private val options: Map<String, Int> = mapOf(
        "All" to R.string.home_label_all,
        "Fighter" to R.string.home_label_fighter,
        "Bomber" to R.string.home_label_bomber,
        "Helicopters" to R.string.home_label_helicopters,
        "Spy" to R.string.home_label_spy,
        "Electronic Warfare" to R.string.home_label_electronic_warfare,
        "Reconnaissance" to R.string.home_label_reconnaissance,
        "Tanker" to R.string.home_label_tanker,
        "Transport" to R.string.home_label_transport,
        "Trainer" to R.string.home_label_trainer,
        "AWACS" to R.string.home_label_awacs,
        "UAVs" to R.string.home_label_uavs,
        "Passenger" to R.string.home_label_passenger,
        "Attack" to R.string.home_label_attack,
        "6th" to R.string.home_label_gen_6,
        "5th" to R.string.home_label_gen_5,
        "4th" to R.string.home_label_gen_4,
        "4.5th" to R.string.home_label_gen_4_5,
        "3th" to R.string.home_label_gen_3,
        "2th" to R.string.home_label_gen_2,
        "Multirole" to R.string.home_label_multirole,
        "Air Superiority" to R.string.home_label_air_superiority,
        "Ground Attack" to R.string.home_label_ground_attack,
        "Precision Strike" to R.string.home_label_precision_strike,
        "Close Air Support (CAS)" to R.string.home_label_cas,
        "Stealth Operations" to R.string.home_label_stealth_ops,
        "Escort" to R.string.home_label_escort,
        "Search and Rescue" to R.string.home_label_sar,
        "Maritime Surveillance" to R.string.home_label_maritime,
        "Active" to R.string.home_label_active,
        "Retired" to R.string.home_label_retired,
        "In Service" to R.string.home_label_in_service,
        "Under Development" to R.string.home_label_under_development,
        "Out of Service" to R.string.home_label_out_of_service,
        "Prototype" to R.string.home_label_prototype,
        "Experimental" to R.string.home_label_experimental,
        "Full Stealth" to R.string.home_label_full_stealth,
        "Reduced Stealth" to R.string.home_label_reduced_stealth,
        "No Stealth" to R.string.home_label_no_stealth,
        "Stealth Features" to R.string.home_label_stealth_features,
        "US Air Force" to R.string.home_label_us_air_force,
        "Royal Air Force" to R.string.home_label_royal_air_force,
        "NATO" to R.string.home_label_nato,
        "<5 Hardpoints" to R.string.home_label_hardpoints_lt5,
        "5–10 Hardpoints" to R.string.home_label_hardpoints_5_10,
        ">10 Hardpoints" to R.string.home_label_hardpoints_gt10,
    )

    /** Flag code (ISO-2) to the translated country name. */
    private val countries: Map<String, Int> = mapOf(
        "af" to R.string.home_country_af,
        "al" to R.string.home_country_al,
        "ao" to R.string.home_country_ao,
        "ar" to R.string.home_country_ar,
        "au" to R.string.home_country_au,
        "at" to R.string.home_country_at,
        "bh" to R.string.home_country_bh,
        "bd" to R.string.home_country_bd,
        "bz" to R.string.home_country_bz,
        "bo" to R.string.home_country_bo,
        "br" to R.string.home_country_br,
        "bn" to R.string.home_country_bn,
        "bg" to R.string.home_country_bg,
        "kh" to R.string.home_country_kh,
        "ca" to R.string.home_country_ca,
        "cf" to R.string.home_country_cf,
        "cn" to R.string.home_country_cn,
        "co" to R.string.home_country_co,
        "km" to R.string.home_country_km,
        "cd" to R.string.home_country_cd,
        "cz" to R.string.home_country_cz,
        "dk" to R.string.home_country_dk,
        "dj" to R.string.home_country_dj,
        "do" to R.string.home_country_do,
        "eg" to R.string.home_country_eg,
        "sv" to R.string.home_country_sv,
        "ee" to R.string.home_country_ee,
        "fi" to R.string.home_country_fi,
        "fr" to R.string.home_country_fr,
        "ge" to R.string.home_country_ge,
        "de" to R.string.home_country_de,
        "gr" to R.string.home_country_gr,
        "hu" to R.string.home_country_hu,
        "in" to R.string.home_country_in,
        "id" to R.string.home_country_id,
        "ir" to R.string.home_country_ir,
        "iq" to R.string.home_country_iq,
        "ie" to R.string.home_country_ie,
        "il" to R.string.home_country_il,
        "it" to R.string.home_country_it,
        "jm" to R.string.home_country_jm,
        "jp" to R.string.home_country_jp,
        "kp" to R.string.home_country_kp,
        "kr" to R.string.home_country_kr,
        "kw" to R.string.home_country_kw,
        "my" to R.string.home_country_my,
        "md" to R.string.home_country_md,
        "mc" to R.string.home_country_mc,
        "me" to R.string.home_country_me,
        "ma" to R.string.home_country_ma,
        "nl" to R.string.home_country_nl,
        "nz" to R.string.home_country_nz,
        "no" to R.string.home_country_no,
        "om" to R.string.home_country_om,
        "pk" to R.string.home_country_pk,
        "ph" to R.string.home_country_ph,
        "pl" to R.string.home_country_pl,
        "pt" to R.string.home_country_pt,
        "qa" to R.string.home_country_qa,
        "ro" to R.string.home_country_ro,
        "ru" to R.string.home_country_ru,
        "sa" to R.string.home_country_sa,
        "rs" to R.string.home_country_rs,
        "za" to R.string.home_country_za,
        "es" to R.string.home_country_es,
        "lk" to R.string.home_country_lk,
        "se" to R.string.home_country_se,
        "ch" to R.string.home_country_ch,
        "tz" to R.string.home_country_tz,
        "th" to R.string.home_country_th,
        "tn" to R.string.home_country_tn,
        "tr" to R.string.home_country_tr,
        "ua" to R.string.home_country_ua,
        "ae" to R.string.home_country_ae,
        "gb" to R.string.home_country_gb,
        "us" to R.string.home_country_us,
        "ve" to R.string.home_country_ve,
        "vn" to R.string.home_country_vn,
    )

    fun optionRes(value: String): Int? = options[value]
    fun countryRes(code: String): Int? = countries[code]
}

/** Translated text of a filter option value or backend category; falls back to the value itself. */
@Composable
fun optionLabel(value: String): String = FilterLabels.optionRes(value)?.let { stringResource(it) } ?: value

/** Translated country name for the filter lists; falls back to the English [FilterCountry.name]. */
@Composable
fun countryLabel(country: FilterCountry): String = FilterLabels.countryRes(country.code)?.let { stringResource(it) } ?: country.name
