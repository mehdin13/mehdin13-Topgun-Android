package app.topgun.android.features.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS View/MainViews/SplashView.swift (Lottie Splashlogo, 3s) */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("SplashScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
