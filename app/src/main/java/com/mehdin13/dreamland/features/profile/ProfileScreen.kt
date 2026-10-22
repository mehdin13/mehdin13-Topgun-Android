package com.mehdin13.dreamland.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mehdin13.dreamland.data.rememberContainer
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLLayout
import com.mehdin13.dreamland.designsystem.DLSpacing
import com.mehdin13.dreamland.designsystem.components.DLButton
import com.mehdin13.dreamland.designsystem.components.DLButtonVariant
import com.mehdin13.dreamland.designsystem.components.DLIconName

/**
 * Profile: sign out only for now. Profile data, Grown-ups settings and account deletion wait on the backend
 * (`/users/me/` is not mounted, BACKEND_GAPS #1, #2, #51, #52); the iOS app shows deletion as "coming soon" too.
 */
@Composable
fun ProfileScreen(contentPadding: PaddingValues) {
    val session = rememberContainer().session
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = DLLayout.screenMarginCompact, end = DLLayout.screenMarginCompact,
                top = contentPadding.calculateTopPadding() + DLSpacing.space8,
                bottom = contentPadding.calculateBottomPadding() + DLSpacing.space32,
            ),
        verticalArrangement = Arrangement.spacedBy(DLSpacing.space16),
    ) {
        Text("Profile", style = DL.type.displayMedium, color = DL.colors.textPrimary, modifier = Modifier.semantics { heading() })
        Text(
            "Account deletion is coming soon. Until then you can sign out of this device.",
            style = DL.type.bodyRegular,
            color = DL.colors.textSecondary,
        )
        // Sign-out only clears this device; the server token stays valid (BACKEND_GAPS #3, #53).
        DLButton("Sign out", onClick = session::signOut, variant = DLButtonVariant.Destructive, icon = DLIconName.SignOut, fullWidth = true)
    }
}
