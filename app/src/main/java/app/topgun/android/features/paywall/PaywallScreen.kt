package app.topgun.android.features.paywall

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGText

/** TODO(port): iOS RevenueCatUI PaywallView sheet; hosts PlatformUi.Paywall */
@Composable
fun PaywallScreen(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("PaywallScreen", style = TGText.sailecMedium(16), color = TG.colors.textPrimary)
    }
}
