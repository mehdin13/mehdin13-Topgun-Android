package app.topgun.core.network

import app.topgun.core.model.Aircraft
import app.topgun.core.model.AircraftDetail
import app.topgun.core.model.AircraftFilter
import app.topgun.core.model.AuthResponse
import app.topgun.core.model.Blog
import app.topgun.core.model.Category
import app.topgun.core.model.Comment
import app.topgun.core.model.Country
import app.topgun.core.model.CustomAircraftList
import app.topgun.core.model.CustomManufacturerList
import app.topgun.core.model.FavoritesResponse
import app.topgun.core.model.LikeResponse
import app.topgun.core.model.Manufacturer
import app.topgun.core.model.Page
import app.topgun.core.model.RateResponse
import app.topgun.core.model.SearchHistoryItem
import app.topgun.core.model.TopGunJson
import app.topgun.core.model.UserInfo
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.FormBody

/**
 * Typed calls against the TopGun API (`/api/v1/`). Paths and shapes follow the Django backend in
 * `ncodestudio/topgun-backend` (apps/users, apps/aircrafts) and what the iOS app sends.
 * Pass `pageSize` up to 100 (the backend's cap); the default page size is 10.
 */
class TopGunApi(private val client: ApiClientContract) {

    // ---------------------------------------------------------------- aircraft

    suspend fun aircraft(page: Int = 1, pageSize: Int = 10): Page<Aircraft> =
        client.send(ApiEndpoint.get("aircrafts/aircraft/", pageQuery(page, pageSize)), Page.serializer(Aircraft.serializer()))

    /** Detail. Throws [ApiError.Forbidden] (`isVipRequired`) for a VIP aircraft when the user is not a Pro member. */
    suspend fun aircraftDetail(id: Int): AircraftDetail =
        client.send(ApiEndpoint.get("aircrafts/aircraft/$id/"), AircraftDetail.serializer())

    suspend fun searchAircraft(query: String, page: Int = 1, pageSize: Int = 10): Page<Aircraft> =
        client.send(
            ApiEndpoint.get("aircrafts/aircraft/aircraft_search/", listOf("aircraft_name" to query) + pageQuery(page, pageSize)),
            Page.serializer(Aircraft.serializer()),
        )

    /** Up to 5 aircraft names plus up to 5 popular searches. */
    suspend fun suggest(query: String): List<String> =
        client.send(ApiEndpoint.get("aircrafts/aircraft/aircraft_suggest/", listOf("q" to query)), ListSerializer(String.serializer()))

    suspend fun filterAircraft(filter: AircraftFilter, page: Int = 1, pageSize: Int = 10): Page<Aircraft> {
        val form = FormBody.Builder().apply { filter.toFormFields().forEach { (k, v) -> add(k, v) } }.build()
        return client.send(
            ApiEndpoint(ApiEndpoint.Method.POST, "aircrafts/aircraft/aircraft_filters/", pageQuery(page, pageSize), ApiEndpoint.Body.Raw(form)),
            Page.serializer(Aircraft.serializer()),
        )
    }

    /** Home category chips use the filter endpoint with only `category` set. */
    suspend fun aircraftByCategory(categoryName: String, page: Int = 1, pageSize: Int = 10): Page<Aircraft> =
        filterAircraft(AircraftFilter(category = categoryName), page, pageSize)

    suspend fun categories(page: Int = 1, pageSize: Int = 100): Page<Category> =
        client.send(ApiEndpoint.get("aircrafts/category/", pageQuery(page, pageSize)), Page.serializer(Category.serializer()))

    /** Sends the star rating (1..5). Needs sign-in. */
    suspend fun rate(aircraftId: Int, rating: Int): RateResponse {
        require(rating in 1..5) { "rating must be 1..5" }
        val body = buildJsonObject { put("rating", rating) }.toString()
        return client.send(ApiEndpoint.postJson("aircrafts/aircraft/$aircraftId/rate/", body), RateResponse.serializer())
    }

    /** Toggles the like. Needs sign-in. */
    suspend fun toggleLike(aircraftId: Int): LikeResponse {
        val body = buildJsonObject { put("aircraft_id", aircraftId) }.toString()
        return client.send(ApiEndpoint.postJson("aircrafts/aircraft-like/", body), LikeResponse.serializer())
    }

    // --------------------------------------------------------------- favorites

    suspend fun favorites(): FavoritesResponse = client.send(ApiEndpoint.get("users/favorites/"), FavoritesResponse.serializer())

    /** The backend toggles: returns true when the aircraft was added (HTTP 201), false when removed (HTTP 200). */
    suspend fun toggleFavorite(aircraftId: Int): Boolean {
        val body = buildJsonObject { put("aircraft_id", aircraftId) }.toString()
        var added = false
        client.send(ApiEndpoint.postJson("users/favorites/", body).copy(onStatus = { added = it == 201 }))
        return added
    }

    // ---------------------------------------------------------------- comments

    suspend fun comments(aircraftId: Int, page: Int = 1): Page<Comment> =
        client.send(
            ApiEndpoint.get("aircrafts/comment/", listOf("aircraft_id" to aircraftId.toString(), "page" to page.toString())),
            Page.serializer(Comment.serializer()),
        )

    /** Needs sign-in. The server answers `{"message": "Created New Comment"}`, so the caller reloads the list. */
    suspend fun postComment(aircraftId: Int, body: String, parentId: Int? = null) {
        val json = buildJsonObject {
            put("aircraft", aircraftId)
            put("body", body)
            if (parentId != null) put("parent", parentId)
        }.toString()
        client.send(ApiEndpoint.postJson("aircrafts/comment/", json))
    }

    // ----------------------------------------------------- countries, makers

    suspend fun countries(page: Int = 1, pageSize: Int = 20, name: String? = null): Page<Country> =
        client.send(
            ApiEndpoint.get("aircrafts/country/", pageQuery(page, pageSize) + listOfNotNull(name?.let { "name" to it })),
            Page.serializer(Country.serializer()),
        )

    suspend fun country(id: Int): Country = client.send(ApiEndpoint.get("aircrafts/country/$id/"), Country.serializer())

    /** Not paginated. */
    suspend fun countriesWithFactories(): List<Country> =
        client.send(ApiEndpoint.get("aircrafts/country/countries_have_factories/"), ListSerializer(Country.serializer()))

    suspend fun manufacturer(id: Int): Manufacturer = client.send(ApiEndpoint.get("aircrafts/manufacturer/$id/"), Manufacturer.serializer())

    suspend fun customAircraftList(name: String): List<CustomAircraftList> =
        client.send(ApiEndpoint.get("aircrafts/custom-aircraft-list/", listOf("name" to name)), ListSerializer(CustomAircraftList.serializer()))

    suspend fun customManufacturerList(name: String = "Top manufacturers"): List<CustomManufacturerList> =
        client.send(ApiEndpoint.get("aircrafts/custom-manufacturer-list/", listOf("name" to name)), ListSerializer(CustomManufacturerList.serializer()))

    suspend fun blogs(page: Int = 1, pageSize: Int = 10): Page<Blog> =
        client.send(ApiEndpoint.get("aircrafts/blog/", pageQuery(page, pageSize)), Page.serializer(Blog.serializer()))

    // ------------------------------------------------------------------- users

    /**
     * Google sign-in. The backend trusts the email we send (it does not verify a Google ID token),
     * which is a security gap on the server (docs/BACKEND_GAPS.md). 201 = new account, 200 = existing.
     */
    suspend fun socialLogin(socialNetwork: String, email: String, firstName: String?, lastName: String?): AuthResponse {
        val body = buildJsonObject {
            put("social_network", socialNetwork)
            put("email", email)
            put("first_name", firstName.orEmpty())
            put("last_name", lastName.orEmpty())
        }.toString()
        return client.send(ApiEndpoint.postJson("users/social_network_register/", body, requiresAuth = false), AuthResponse.serializer())
    }

    suspend fun userInfo(): UserInfo = client.send(ApiEndpoint.get("users/user_info/"), UserInfo.serializer())

    /** Revokes the token on the server. Call before clearing the local session. */
    suspend fun logout() = client.send(ApiEndpoint.post("users/logout/"))

    /**
     * Marks the user as Pro for [days] days after a purchase. The backend does not require sign-in for this call
     * and does not check the purchase (docs/BACKEND_GAPS.md); the RevenueCat webhook is the safe path.
     */
    suspend fun makeVip(userId: Int, days: Int) {
        val body = buildJsonObject { put("user_id", userId); put("vip_days_remaining", days) }.toString()
        client.send(ApiEndpoint.postJson("users/make_vip/", body))
    }

    suspend fun searchHistory(): Page<SearchHistoryItem> =
        client.send(ApiEndpoint.get("users/search-history/"), Page.serializer(SearchHistoryItem.serializer()))

    suspend fun addSearchHistory(content: String) {
        val body = buildJsonObject { put("content", content) }.toString()
        client.send(ApiEndpoint.postJson("users/search-history/", body))
    }

    suspend fun deleteSearchHistory(id: Int) = client.send(ApiEndpoint.delete("users/search-history/$id/"))

    suspend fun clearSearchHistory() = client.send(ApiEndpoint.delete("users/search-history/clear/"))

    private fun pageQuery(page: Int, pageSize: Int) = listOf("page" to page.toString(), "page_size" to pageSize.toString())
}
