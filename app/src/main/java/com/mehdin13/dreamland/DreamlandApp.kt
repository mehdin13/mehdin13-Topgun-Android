package com.mehdin13.dreamland

import android.app.Application
import com.mehdin13.dreamland.data.AppContainer

class DreamlandApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
