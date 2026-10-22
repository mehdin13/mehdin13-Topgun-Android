package app.topgun.core

import app.topgun.core.model.Aircraft
import app.topgun.core.model.AircraftDetail
import app.topgun.core.model.AuthResponse
import app.topgun.core.model.Country
import app.topgun.core.model.Manufacturer
import app.topgun.core.model.Page
import app.topgun.core.model.TopGunJson
import app.topgun.core.model.UserInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelDecodingTest {
    private val listJson = """
        {"count": 42, "next": "https://api.example.com/api/v1/aircrafts/aircraft/?page=2", "previous": null, "results": [
          {"id": 1, "name": "F-22 Raptor", "role": "Air superiority fighter",
           "manufacturers": [{"id": 3, "country": "United States", "name": "Lockheed Martin", "logo": "/media/logos/lm.png", "cover": null,
                              "description": null, "wikipedia_link": null, "website_link": "https://lockheedmartin.com"}],
           "countries": [{"id": 9, "code": "US", "name": "United States", "icon": null, "flag_icon": "/media/icons/us.png"}],
           "category": {"id": 2, "name": "Fighter", "icon": "/media/icons/fighter.svg", "custom_fields": ["Ordnance"]},
           "custom_fields": [{"field_name": "Ordnance", "field_value": "AIM-120"}],
           "average_rating": 4.5, "introduction": "2005", "national_origin": "USA", "thumbnail": "/media/aircraft_thumbnails/f22.jpg",
           "crew_size": 1, "price_million": "150.00", "generation": "5th", "status": "Active", "primary_user": "USAF",
           "produced": "1997-2011", "number_built": 195, "file_3D": "/media/file_3D/f22.usdz", "youtube_video": null,
           "is_vip": true, "is_favorite": true, "likes_count": 12}
        ]}
    """.trimIndent()

    @Test fun decodesAnAircraftPage() {
        val page = TopGunJson.decodeFromString(Page.serializer(Aircraft.serializer()), listJson)
        assertEquals(42, page.count)
        assertTrue(page.hasNext)
        val a = page.results.single()
        assertEquals("F-22 Raptor", a.name)
        assertEquals("150.00", a.priceMillion)
        // number_built arrives as a number from some rows and a string from others.
        assertEquals("195", a.numberBuilt)
        assertEquals("/media/file_3D/f22.usdz", a.file3d)
        assertEquals("Ordnance", a.customFields.single().fieldName)
        assertEquals("Lockheed Martin", a.manufacturers.single().name)
        assertEquals("United States", a.manufacturers.single().country)
        assertTrue(a.isVip && a.isFavorite)
        assertNull(a.youtubeVideo)
    }

    @Test fun missingAndNullFieldsUseDefaults() {
        val a = TopGunJson.decodeFromString(Aircraft.serializer(), """{"id": 5, "name": null, "is_vip": null, "likes_count": null, "category": null}""")
        assertEquals("", a.name)
        assertFalse(a.isVip)
        assertEquals(0, a.likesCount)
        assertNull(a.category)
    }

    @Test fun decodesAnAircraftDetailWithNestedMedia() {
        val d = TopGunJson.decodeFromString(
            AircraftDetail.serializer(),
            """
            {"id": 7, "name": "A-10", "price_million": 18.8, "autonomy_hours": "4.50", "max_speed": "706", "first_flight": "1972-05-10",
             "developed_from": null, "mission_roles": ["CAS", "COIN"], "service_ceiling": 13636.0, "engine_afterburner": false,
             "file_3D": null, "file_pdf": "/media/file_pdf/a10.pdf", "main_image": "https://api.example.com/media/main_images/a10.jpg",
             "image_gallery": [{"id": 1, "image": "/media/gallery/images/1.jpg", "is_portrait": true}],
             "video_gallery": [{"id": 2, "video": "/media/gallery/videos/1.mp4"}],
             "sketches": [{"id": 3, "name": "Side", "file": "/media/sketch/s.png"}],
             "variants": [], "similar": [{"id": 8, "name": "Su-25"}],
             "countries_of_operation": [{"id": 1, "name": "Greece", "code": "GR", "aircraft_count": 3, "flag_icon": null, "capital": "Athens", "lat": "37.9838", "long": 23.7275}],
             "comments": [{"id": 1, "email": "a@b.c", "first_name": "A", "last_name": "B", "content": "Brrrt", "created_at": "2026-01-01T10:00:00Z"}],
             "is_favorite": false, "likes_count": 4, "average_rating": 4.0}
            """.trimIndent(),
        )
        assertEquals("18.8", d.priceMillion)
        assertEquals("4.50", d.autonomyHours)
        assertNull(d.file3d)
        assertEquals("/media/file_pdf/a10.pdf", d.filePdf)
        assertTrue(d.imageGallery.single().isPortrait)
        assertEquals("Su-25", d.similar.single().name)
        val greece = d.countriesOfOperation.single()
        assertEquals(37.9838, greece.latitude)
        assertEquals(23.7275, greece.longitude)
        assertEquals("Brrrt", d.comments.single().content)
        assertEquals(listOf("CAS", "COIN"), d.missionRoles)
    }

    @Test fun decodesCountriesAndManufacturers() {
        val c = TopGunJson.decodeFromString(
            Country.serializer(),
            """{"id": 1, "name": "Japan", "code": "JP", "flag_icon": "/media/f.png", "aircraft_count": 2, "lat": "35.6762", "long": "139.6503",
                "air_force_founded": "1954-07-01", "aircraft_list": [{"id": 4, "name": "F-2", "thumbnail": null, "national_origin": "Japan", "role": "Multirole", "category": "Fighter"}],
                "manufacturers": [{"id": 6, "name": "Mitsubishi", "logo": null, "cover": null, "wikipedia_link": null, "website_link": null}]}""",
        )
        assertEquals("Japan", c.name)
        assertEquals(35.6762, c.latitude)
        assertEquals("F-2", c.aircraftList.single().name)
        assertEquals("Mitsubishi", c.manufacturers.single().name)

        val m = TopGunJson.decodeFromString(
            Manufacturer.serializer(),
            """{"id": 6, "country": {"id": 1, "code": "JP", "name": "Japan", "icon": null, "flag_icon": null}, "name": "Mitsubishi",
                "aircraft_list": [{"id": 4, "name": "F-2", "national_origin": "Japan", "role": "Multirole", "category": "Fighter"}]}""",
        )
        assertEquals("Japan", m.country?.name)
        assertEquals(1, m.aircraftList.size)
    }

    @Test fun decodesAuthAndProfile() {
        val auth = TopGunJson.decodeFromString(AuthResponse.serializer(), """{"message":"Login successful.","user_id":9,"token":"abc","is_vip":false}""")
        assertEquals("abc", auth.token)
        assertEquals(9, auth.userId)

        val info = TopGunJson.decodeFromString(
            UserInfo.serializer(),
            """{"user_id": 9, "email": "a@b.c", "is_active": true, "first_name": "Ann", "last_name": "Lee", "avatar": null,
                "badges": [{"id": 1, "name": "Airman", "icon": "/media/b.png", "cover": null, "is_have": true}, {"id": 2, "name": "Ace", "icon": null, "cover": null, "is_have": false}],
                "social_network": "google", "region": "us", "is_vip": true, "vip_days_remaining": 180, "join_date": "2026-01-01T00:00:00Z", "modified_at": "2026-01-02T00:00:00Z"}""",
        )
        assertTrue(info.isVip)
        assertEquals(180, info.vipDaysRemaining)
        assertEquals(listOf(true, false), info.badges.map { it.isHave })
    }
}
