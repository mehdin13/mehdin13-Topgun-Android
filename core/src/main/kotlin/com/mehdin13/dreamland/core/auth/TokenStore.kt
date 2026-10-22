package com.mehdin13.dreamland.core.auth

import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persists the auth token. The Android app backs this with encrypted storage; tests use [InMemoryTokenStore]. */
interface TokenStore {
    fun read(): String?
    fun save(token: String)
    fun clear()
}

class InMemoryTokenStore(initial: String? = null) : TokenStore {
    private val value = AtomicReference(initial)
    override fun read(): String? = value.get()
    override fun save(token: String) = value.set(token)
    override fun clear() = value.set(null)
}

/** Holds the signed-in state. A confirmed 401 on an authenticated request calls [signOut]. Tokens are never logged. */
class SessionStore(private val store: TokenStore) {
    private val _isSignedIn = MutableStateFlow(store.read() != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    fun token(): String? = store.read()

    fun signIn(token: String) {
        store.save(token)
        _isSignedIn.value = true
    }

    fun signOut() {
        store.clear()
        _isSignedIn.value = false
    }
}
