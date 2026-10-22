package app.topgun.core

import app.topgun.core.network.ApiConfiguration
import app.topgun.core.network.MediaUrlResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaUrlResolverTest {
    private val config = ApiConfiguration("https://api.example.com/api/v1")
    private val resolver = MediaUrlResolver(config)

    @Test fun addsTrailingSlashAndDerivesOrigin() {
        assertEquals("https://api.example.com/api/v1/", config.baseUrl.toString())
        assertEquals("https://api.example.com/", config.origin.toString())
    }

    @Test fun resolvesRelativePathsAgainstTheOrigin() {
        assertEquals("https://api.example.com/media/aircraft_thumbnails/f16.jpg", resolver.resolve("/media/aircraft_thumbnails/f16.jpg"))
        assertEquals("https://api.example.com/media/a.glb", resolver.resolve("media/a.glb"))
    }

    @Test fun passesAbsoluteHttpUrlsThrough() {
        assertEquals("https://cdn.example.com/a.png", resolver.resolve("https://cdn.example.com/a.png"))
        assertEquals("http://10.0.2.2:8000/media/a.png", resolver.resolve(" http://10.0.2.2:8000/media/a.png "))
    }

    @Test fun rejectsMissingEmptyAndNonHttpValues() {
        assertNull(resolver.resolve(null))
        assertNull(resolver.resolve(""))
        assertNull(resolver.resolve("   "))
        assertNull(resolver.resolve("file:///etc/passwd"))
        assertNull(resolver.resolve("javascript:alert(1)"))
    }

    @Test fun keepsNonDefaultPortInOrigin() {
        assertEquals("http://10.0.2.2:8000/media/x.png", MediaUrlResolver(ApiConfiguration("http://10.0.2.2:8000/api/v1/")).resolve("/media/x.png"))
    }
}
