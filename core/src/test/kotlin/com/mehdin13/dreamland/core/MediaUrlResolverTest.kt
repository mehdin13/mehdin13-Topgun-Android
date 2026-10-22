package com.mehdin13.dreamland.core

import com.mehdin13.dreamland.core.network.ApiConfiguration
import com.mehdin13.dreamland.core.network.MediaUrlResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaUrlResolverTest {
    private val config = ApiConfiguration("https://dreamland.example.com/api")
    private val resolver = MediaUrlResolver(config)

    @Test fun addsTrailingSlashAndDerivesOrigin() {
        assertEquals("https://dreamland.example.com/api/", config.baseUrl.toString())
        assertEquals("https://dreamland.example.com/", config.origin.toString())
    }

    @Test fun resolvesRelativePathsAgainstTheOrigin() {
        assertEquals("https://dreamland.example.com/media/stories/covers/x.png", resolver.resolve("/media/stories/covers/x.png"))
        assertEquals("https://dreamland.example.com/media/a.mp3", resolver.resolve("media/a.mp3"))
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
        val local = MediaUrlResolver(ApiConfiguration("http://10.0.2.2:8000/api/"))
        assertEquals("http://10.0.2.2:8000/media/x.png", local.resolve("/media/x.png"))
    }
}
