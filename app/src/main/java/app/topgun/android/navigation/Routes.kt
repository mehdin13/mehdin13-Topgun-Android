package app.topgun.android.navigation

/** Navigation routes. Arguments are plain path segments so any screen can build a route without a NavController. */
object Routes {
    const val Gate = "gate"
    const val Main = "main"
    const val SignIn = "signin"
    const val Paywall = "paywall"
    const val Countries = "countries"
    const val Manufacturers = "manufacturers"
    const val Blog = "blog"
    const val Badges = "badges"

    const val AircraftPattern = "aircraft/{id}"
    const val CountryPattern = "country/{id}"
    const val ManufacturerPattern = "manufacturer/{id}"
    const val ListPattern = "list/{name}"

    fun aircraft(id: Int) = "aircraft/$id"
    fun country(id: Int) = "country/$id"
    fun manufacturer(id: Int) = "manufacturer/$id"
    /** [name] is the backend list name, for example "Top WWII" (URL-encoded by the caller). */
    fun list(name: String) = "list/${java.net.URLEncoder.encode(name, "UTF-8")}"
}
