package app.topgun.android.data

/** Languages the UI is translated into. [code] is a BCP-47 language tag used for resources and the `Accept-Language` header. */
enum class AppLanguage(val code: String, val nativeName: String, val rtl: Boolean = false) {
    English("en", "English"),
    Persian("fa", "فارسی", rtl = true),
    Arabic("ar", "العربية", rtl = true),
    Spanish("es", "Español"),
    French("fr", "Français"),
    German("de", "Deutsch"),
    Turkish("tr", "Türkçe"),
    Russian("ru", "Русский");

    companion object {
        fun fromCode(code: String?): AppLanguage? = entries.firstOrNull { it.code.equals(code, ignoreCase = true) }

        /** The supported language closest to the device language, English when none matches. */
        fun matching(deviceLanguage: String): AppLanguage = fromCode(deviceLanguage) ?: English
    }
}
