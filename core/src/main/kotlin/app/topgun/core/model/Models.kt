@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package app.topgun.core.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/** DRF PageNumberPagination envelope: `{count, next, previous, results}`. */
@Serializable
data class Page<T>(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<T> = emptyList(),
) {
    val hasNext: Boolean get() = next != null
}

@Serializable
data class Category(
    val id: Int,
    val name: String = "",
    val icon: String? = null,
    val customFields: List<String> = emptyList(),
)

/** Country as nested inside an aircraft (CountryForAircraftSerializer). */
@Serializable
data class CountryBrief(
    val id: Int,
    val code: String? = null,
    val name: String = "",
    val icon: String? = null,
    val flagIcon: String? = null,
)

/** Manufacturer as nested inside an aircraft. `country` is the country's name, not an object. */
@Serializable
data class ManufacturerBrief(
    val id: Int,
    val country: String? = null,
    val name: String = "",
    val logo: String? = null,
    val cover: String? = null,
    val description: String? = null,
    val wikipediaLink: String? = null,
    val websiteLink: String? = null,
)

@Serializable
data class CustomField(val fieldName: String? = null, val fieldValue: String? = null)

/** A row of the aircraft list, search, filter, favorites and custom lists (AircraftSerializer). Also used for `variants` and `similar`. */
@Serializable
data class Aircraft(
    val id: Int,
    val name: String = "",
    val role: String? = null,
    val manufacturers: List<ManufacturerBrief> = emptyList(),
    val countries: List<CountryBrief> = emptyList(),
    val category: Category? = null,
    val customFields: List<CustomField> = emptyList(),
    val averageRating: Double? = null,
    val introduction: String? = null,
    val nationalOrigin: String? = null,
    val thumbnail: String? = null,
    val crewSize: Int? = null,
    @Serializable(with = FlexString::class) val priceMillion: String? = null,
    val generation: String? = null,
    val status: String? = null,
    val primaryUser: String? = null,
    @Serializable(with = FlexString::class) val produced: String? = null,
    @Serializable(with = FlexString::class) val numberBuilt: String? = null,
    @JsonNames("file_3D") val file3d: String? = null,
    /** Android-loadable glb conversion of [file3d] (backend field `file_3D_glb`, serializer variants accepted). */
    @JsonNames("file_3D_glb", "file_3d_glb") val fileGlb: String? = null,
    val youtubeVideo: String? = null,
    /** VIP aircraft open only for Pro members (403 from the detail endpoint otherwise). */
    val isVip: Boolean = false,
    val isFavorite: Boolean = false,
    val likesCount: Int = 0,
)

@Serializable
data class ImageGalleryItem(val id: Int, val image: String = "", val isPortrait: Boolean = false)

@Serializable
data class VideoGalleryItem(val id: Int, val video: String = "")

@Serializable
data class Sketch(val id: Int, val name: String = "", val file: String? = null)

/** Country of operation on the aircraft detail, with coordinates for the map. */
@Serializable
data class CountryOfOperation(
    val id: Int,
    val name: String? = null,
    val code: String? = null,
    val aircraftCount: Int = 0,
    val flagIcon: String? = null,
    val capital: String? = null,
    @Serializable(with = FlexString::class) val lat: String? = null,
    @Serializable(with = FlexString::class) val long: String? = null,
) {
    val latitude: Double? get() = lat?.toDoubleOrNull()
    val longitude: Double? get() = long?.toDoubleOrNull()
}

/** A comment embedded in the detail response (AircraftComment). */
@Serializable
data class EmbeddedComment(
    val id: Int,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val content: String = "",
    val createdAt: String = "",
)

/** `GET aircraft/{id}/` (DetailAircraftSerializer, `fields = '__all__'`). Almost every field can be null or missing. */
@Serializable
data class AircraftDetail(
    val id: Int,
    val name: String = "",
    val nationalOrigin: String? = null,
    @Serializable(with = FlexString::class) val priceMillion: String? = null,
    val primaryRole: String? = null,
    val role: String? = null,
    val missionRoles: List<String> = emptyList(),
    val crewSize: Int? = null,
    val trustToWeightRatio: String? = null,
    val stealthCapabilities: String? = null,
    val generation: String? = null,
    val firstFlight: String? = null,
    val description: String? = null,
    val wikipediaLink: String? = null,
    val developedFrom: Int? = null,
    val introduction: String? = null,
    val fuelCapacity: String? = null,
    val emptyWeight: String? = null,
    val maxWeight: String? = null,
    val maxPayload: String? = null,
    val length: String? = null,
    val height: String? = null,
    val wingspan: String? = null,
    val wingConfiguration: String? = null,
    val maxSpeed: String? = null,
    val range: String? = null,
    val maxGLoadGs: String? = null,
    val combatRadius: String? = null,
    val ferryRange: String? = null,
    val operationalRange: String? = null,
    val radarSystem: String? = null,
    val radarRange: String? = null,
    val propulsion: String? = null,
    val electronicCountermeasures: String? = null,
    val radarWarningReceiver: String? = null,
    val electronicSupportMeasures: String? = null,
    val jammingSystems: String? = null,
    val engineType: String? = null,
    val numberOfEngines: Int? = null,
    val engineAfterburner: Boolean? = null,
    @Serializable(with = FlexString::class) val autonomyHours: String? = null,
    val armament: String? = null,
    val flyByWire: Boolean? = null,
    val natoReportingName: String? = null,
    val hardpoints: Int? = null,
    val canopyType: String? = null,
    val irst: String? = null,
    val maxTakeoffWeight: Double? = null,
    val grossWeight: String? = null,
    val wingArea: String? = null,
    val wingLoading: String? = null,
    val serviceCeiling: Double? = null,
    val radarCrossSection: Double? = null,
    val takeoffDistance: Double? = null,
    val landingDistance: Double? = null,
    val rateOfClimb: Double? = null,
    val lifecycleCost: Double? = null,
    val airfoil: String? = null,
    val aircraftClass: String? = null,
    val combatRange: Double? = null,
    val trivia: String? = null,
    val royalWingman: Boolean? = null,
    val mainImage: String? = null,
    val takeoffDistances: Double? = null,
    val landingDistances: Double? = null,
    val thumbnail: String? = null,
    val icon: String? = null,
    @JsonNames("file_3D") val file3d: String? = null,
    /** Android-loadable glb conversion of [file3d] (backend field `file_3D_glb`, serializer variants accepted). */
    @JsonNames("file_3D_glb", "file_3d_glb") val fileGlb: String? = null,
    val filePdf: String? = null,
    val youtubeVideo: String? = null,
    val status: String? = null,
    val primaryUser: String? = null,
    @Serializable(with = FlexString::class) val produced: String? = null,
    @Serializable(with = FlexString::class) val numberBuilt: String? = null,
    val isVip: Boolean = false,
    val isFavorite: Boolean = false,
    val likesCount: Int = 0,
    val averageRating: Double? = null,
    val category: Category? = null,
    val countries: List<CountryBrief> = emptyList(),
    val manufacturers: List<ManufacturerBrief> = emptyList(),
    val customFields: List<CustomField> = emptyList(),
    val imageGallery: List<ImageGalleryItem> = emptyList(),
    val videoGallery: List<VideoGalleryItem> = emptyList(),
    val sketches: List<Sketch> = emptyList(),
    val variants: List<Aircraft> = emptyList(),
    val similar: List<Aircraft> = emptyList(),
    val countriesOfOperation: List<CountryOfOperation> = emptyList(),
    val comments: List<EmbeddedComment> = emptyList(),
)

/** Row in a country's `aircraft_list`. */
@Serializable
data class CountryAircraftRow(
    val id: Int,
    val name: String = "",
    val thumbnail: String? = null,
    val nationalOrigin: String? = null,
    val role: String? = null,
    val category: String? = null,
)

/** Row in a manufacturer's `aircraft_list` (no thumbnail). */
@Serializable
data class ManufacturerAircraftRow(
    val id: Int,
    val name: String? = null,
    val nationalOrigin: String? = null,
    val role: String? = null,
    val category: String? = null,
)

@Serializable
data class ManufacturerSimple(
    val id: Int,
    val name: String = "",
    val logo: String? = null,
    val cover: String? = null,
    val wikipediaLink: String? = null,
    val websiteLink: String? = null,
)

/** `country/` rows and `country/{id}/` (CountrySerializer, `fields = '__all__'`). */
@Serializable
data class Country(
    val id: Int,
    val name: String = "",
    val code: String? = null,
    val icon: String? = null,
    val flagIcon: String? = null,
    val cover: String? = null,
    val description: String? = null,
    val capital: String? = null,
    val rank: String? = null,
    val primaryBomber: String? = null,
    val aircraftCount: Int = 0,
    val airForceLink: String? = null,
    val airForceFounded: String? = null,
    @Serializable(with = FlexString::class) val lat: String? = null,
    @Serializable(with = FlexString::class) val long: String? = null,
    val aircraftList: List<CountryAircraftRow> = emptyList(),
    val manufacturers: List<ManufacturerSimple> = emptyList(),
) {
    val latitude: Double? get() = lat?.toDoubleOrNull()
    val longitude: Double? get() = long?.toDoubleOrNull()
}

/** `manufacturer/` rows, `manufacturer/{id}/` and the items of a custom manufacturer list. */
@Serializable
data class Manufacturer(
    val id: Int,
    val country: CountryBrief? = null,
    val name: String = "",
    val logo: String? = null,
    val cover: String? = null,
    val description: String? = null,
    val wikipediaLink: String? = null,
    val websiteLink: String? = null,
    val aircraftList: List<ManufacturerAircraftRow> = emptyList(),
)

/** `custom-aircraft-list/?name=...` -> a list of these (name + aircraft). */
@Serializable
data class CustomAircraftList(val name: String = "", val aircraftList: List<Aircraft> = emptyList())

@Serializable
data class CustomManufacturerList(val name: String = "", val manufacturerList: List<Manufacturer> = emptyList())

@Serializable
data class Blog(
    val id: Int,
    val title: String = "",
    val subHeader: String? = null,
    val description: String = "",
    val image: String? = null,
    val source: String = "",
    val sourceLogo: String? = null,
    val created: String = "",
)

/** Comment from `comment/?aircraft_id=` (Comment model with threaded `replies`). */
@Serializable
data class Comment(
    val id: Int,
    val user: Int = 0,
    val aircraft: Int = 0,
    val parent: Int? = null,
    val body: String = "",
    val isActive: Boolean = true,
    val created: String = "",
    val replies: List<Comment> = emptyList(),
)

@Serializable
data class Badge(
    val id: Int,
    val name: String? = null,
    val icon: String? = null,
    val cover: String? = null,
    /** True when the signed-in user has earned it. */
    val isHave: Boolean = false,
)

/** `users/user_info/` (ProfileSerializer). */
@Serializable
data class UserInfo(
    val userId: Int = 0,
    val email: String = "",
    val isActive: Boolean = true,
    val firstName: String = "",
    val lastName: String = "",
    val avatar: String? = null,
    val badges: List<Badge> = emptyList(),
    val socialNetwork: String? = null,
    /** Two-letter language/region code (`en`, `fr`, ...): the API translates text for non-English regions. */
    val region: String = "us",
    val isVip: Boolean = false,
    val vipDaysRemaining: Int = 0,
    val joinDate: String? = null,
    val modifiedAt: String? = null,
)

/** Response of the social login, email login and verify endpoints. */
@Serializable
data class AuthResponse(
    val message: String? = null,
    val userId: Int? = null,
    val token: String? = null,
    val isVip: Boolean = false,
)

/** `users/favorites/` GET: all favorite aircraft grouped under the user's email. */
@Serializable
data class FavoritesResponse(val user: String = "", val aircrafts: List<Aircraft> = emptyList())

@Serializable
data class SearchHistoryItem(val id: Int, val content: String = "", val createdAt: String = "")

@Serializable
data class RateResponse(val message: String? = null, val rating: Int? = null, val averageRating: Double? = null)

@Serializable
data class LikeResponse(val status: String = "") {
    val isLiked: Boolean get() = status == "liked"
}
