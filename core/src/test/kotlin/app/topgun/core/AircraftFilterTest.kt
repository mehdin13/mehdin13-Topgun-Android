package app.topgun.core

import app.topgun.core.model.AircraftFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AircraftFilterTest {
    @Test fun defaultsSendNothing() {
        assertTrue(AircraftFilter().isEmpty)
        assertTrue(AircraftFilter(priceRange = 100.0, range = 1000.0, introYear = 1940.0, crewSize = 1, hasElectronicWarfare = false, isVip = false).isEmpty)
    }

    @Test fun usesTheFieldNamesAndFormatsOfTheIosApp() {
        val fields = AircraftFilter(
            category = "Fighter",
            generations = listOf("4th", "5th"),
            roles = listOf("Multirole"),
            priceRange = 85.0,
            range = 600.0,
            primaryUsers = listOf("US Navy", "USAF"),
            crewSize = 2,
            isVip = true,
        ).toFormFields()
        assertEquals("Fighter", fields["category"])
        assertEquals("4th,5th", fields["generation"])
        assertEquals("Multirole", fields["role"])
        assertEquals("0,85", fields["price_range"])
        assertEquals("0,600", fields["range"])
        assertEquals("US Navy,USAF", fields["primary_user"])
        assertEquals("2", fields["crew_size"])
        // iOS sends "true", which the backend ignores; "vip" is what its filter understands.
        assertEquals("vip", fields["vip_status"])
    }
}
