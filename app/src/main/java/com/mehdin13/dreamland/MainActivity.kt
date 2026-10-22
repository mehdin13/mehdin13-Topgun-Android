package com.mehdin13.dreamland

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mehdin13.dreamland.designsystem.DreamlandTheme
import com.mehdin13.dreamland.navigation.DreamlandRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamlandTheme { DreamlandRoot() }
        }
    }
}
