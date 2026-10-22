package com.mehdin13.dreamland.core

import com.mehdin13.dreamland.core.auth.InMemoryTokenStore
import com.mehdin13.dreamland.core.auth.SessionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionStoreTest {
    @Test fun startsSignedOutWithoutToken() = assertFalse(SessionStore(InMemoryTokenStore()).isSignedIn.value)

    @Test fun signInPersistsAndSignOutClears() {
        val store = InMemoryTokenStore()
        val session = SessionStore(store)
        session.signIn("t")
        assertTrue(session.isSignedIn.value)
        assertEquals("t", SessionStore(store).token())
        session.signOut()
        assertFalse(session.isSignedIn.value)
        assertEquals(null, store.read())
    }
}
