package app.topgun.android.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.R
import app.topgun.android.data.LocalAppContainer
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountColors
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.TGImage
import app.topgun.android.designsystem.largeTitle

/** Links and addresses from iOS `SettingsView` / `SupportEmail`. */
internal object SettingsLinks {
    const val WEBSITE = "https://topgunapp.com"
    const val PRIVACY = "https://topgunapp.com/Privacy"
    const val INSTAGRAM = "https://www.instagram.com/topgunapp"
    const val TWITTER = "https://twitter.com/topgunapp"
    const val SUPPORT_EMAIL = "Topgunapp@gmail.com"
    const val SUPPORT_SUBJECT = "Support email from app user"
    const val FEATURE_SUBJECT = "Feature request"
    /** The Play listing is not published yet: assumes the listing uses the application id. */
    const val SHARE_TEXT = "Check out this amazing app! Download it now from Google Play.\nhttps://play.google.com/store/apps/details?id=app.topgun.android"
}

/**
 * iOS `SettingsView` (the Settings tab): profile header, then General / Preference / Rate / Support / Theme / Account.
 * "Change Theme" opens [ThemeScreen] in place (the tab has no navigation controller of its own).
 */
@Composable
fun SettingsScreen(onOpenBlog: () -> Unit, onOpenBadges: () -> Unit, onShowSignIn: () -> Unit, onShowPaywall: () -> Unit) {
    val container = LocalAppContainer.current
    val vm = containerViewModel { SettingsViewModel(it.user, it.prefs, it.platform) }
    val user by container.user.state.collectAsStateWithLifecycle()
    val notifications by container.prefs.notificationsEnabled.collectAsStateWithLifecycle()
    val cache by vm.cache.collectAsStateWithLifecycle()
    val restoring by vm.restoring.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val platform = container.platform

    var showTheme by rememberSaveable { mutableStateOf(false) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val deleting by vm.deleting.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    BackHandler(enabled = showTheme) { showTheme = false }
    LaunchedEffect(Unit) { vm.refreshCache() }          // iOS `.onAppear { updateCacheInfo() }`
    LaunchedEffect(toast) {
        toast?.let { snackbar.showSnackbar(it); vm.dismissToast() }
    }

    if (showTheme) {
        ThemeScreen(onBack = { showTheme = false }, onSelect = vm::setAppearance)
        return
    }

    val cacheSize = SettingsViewModel.formatBytes(cache.bytes)
    Box(Modifier.fillMaxSize().background(settingsBackground())) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(bottom = TGSpace.tabBarClearance),
            verticalArrangement = Arrangement.spacedBy(TGSpace.l),
        ) {
            Text("Settings", style = TGText.largeTitle, color = TG.colors.textPrimary, modifier = Modifier.padding(horizontal = TGSpace.l, vertical = TGSpace.s))
            ProfileHeader(
                name = user.firstName?.takeIf { it.isNotBlank() } ?: "TopGun",
                email = user.email.takeIf { user.isLoggedIn },
                avatar = user.info?.avatar,
                signedIn = user.isLoggedIn,
                onSignIn = onShowSignIn,
            )

            SettingsSection("General") {
                SettingsRow(
                    Icons.Filled.NotificationsActive, TGAccountColors.systemGreen, "Enable Notifications",
                    onClick = { vm.setNotifications(!notifications) },
                    trailing = { Switch(checked = notifications, onCheckedChange = vm::setNotifications) },
                )
                SettingsRow(Icons.Filled.Verified, TGBrand.orange, "Blog", onClick = onOpenBlog, trailing = { SettingsChevron() })
                SettingsRow(
                    Icons.Filled.WorkspacePremium, TGAccountColors.systemYellow, "Badges", onClick = onOpenBadges, trailing = { SettingsChevron() },
                    showDivider = !user.isPremium,
                )
                if (!user.isPremium) {
                    SettingsRow(
                        Icons.Filled.Verified, TGBrand.blue, "Get Topgun Pro",
                        onClick = { if (user.isLoggedIn) onShowPaywall() else onShowSignIn() },
                    )
                    SettingsRow(
                        Icons.Filled.Restore, TGBrand.blue, "Restore Purchases", showDivider = false,
                        subtitle = if (restoring) "Restoring..." else null,
                        onClick = { if (user.isLoggedIn) vm.restorePurchases() else onShowSignIn() },
                    )
                }
            }

            SettingsSection("Preference") {
                SettingsRow(
                    Icons.Filled.Translate, TGAccountColors.systemPurple, "Language",
                    trailing = { Text("English", style = TGText.sailecRegular(17), color = TG.colors.textSecondary) },
                )
                SettingsRow(
                    Icons.Filled.Inventory2, TGBrand.orange, "Clear Cache", subtitle = "${cache.fileCount} files • $cacheSize",
                    showDivider = false, onClick = { confirmClear = true },
                )
            }

            SettingsSection("Rate! ✨") {
                SettingsRow(Icons.Filled.Star, TGAccountColors.systemYellow, "Rate Us", onClick = platform::openRateApp)
                SettingsRow(
                    Icons.Filled.Share, TGAccountColors.systemPurple, "Share with Friends", titleColor = TGBrand.blue, showDivider = false,
                    onClick = { platform.share(SettingsLinks.SHARE_TEXT) },
                )
            }

            SettingsSection("Support✨") {
                SettingsRow(Icons.Filled.Language, TGBrand.blue, "Website", onClick = { platform.openUrl(SettingsLinks.WEBSITE) })
                SettingsRow(Icons.Filled.Email, TGAccountColors.systemGreen, "Support", onClick = { platform.sendEmail(SettingsLinks.SUPPORT_EMAIL, SettingsLinks.SUPPORT_SUBJECT) })
                SettingsRow(Icons.Filled.PrivacyTip, TGBrand.red, "Privacy Policy", onClick = { platform.openUrl(SettingsLinks.PRIVACY) })
                // iOS opens the WishKit feature-request board; there is no Android equivalent, so this writes to support.
                SettingsRow(
                    Icons.Filled.AutoFixHigh, TGBrand.blue, "Request feature", showDivider = false,
                    onClick = { platform.sendEmail(SettingsLinks.SUPPORT_EMAIL, SettingsLinks.FEATURE_SUBJECT) },
                )
            }

            // iOS also has "Change App icon" here; Android cannot switch the launcher icon at runtime, so it is left out.
            SettingsSection("Theme") {
                SettingsRow(Icons.Filled.Palette, TGAccountColors.systemYellow, "Change Theme", showDivider = false, onClick = { showTheme = true }, trailing = { SettingsChevron() })
            }

            SettingsSection("Account") {
                SettingsRow(Icons.Filled.WbSunny, TGAccountColors.systemPurple, "Instagram", onClick = { platform.openUrl(SettingsLinks.INSTAGRAM) })
                SettingsRow(Icons.Filled.Cancel, TG.colors.textPrimary, "Twitter", showDivider = user.isLoggedIn, onClick = { platform.openUrl(SettingsLinks.TWITTER) })
                if (user.isLoggedIn) {
                    SettingsRow(
                        Icons.AutoMirrored.Filled.Logout, TGBrand.orange, "Logout", titleColor = TGBrand.red,
                        onClick = { confirmLogout = true },
                    )
                    // Not in the iOS app yet; Google Play requires an in-app way to delete the account.
                    SettingsRow(
                        Icons.Filled.Delete, TGBrand.red, "Delete Account", titleColor = TGBrand.red, showDivider = false,
                        onClick = { if (!deleting) confirmDelete = true },
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = TGSpace.tabBarClearance))
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear 3D Models Cache?") },
            text = { Text("This will delete all ${cache.fileCount} downloaded 3D models ($cacheSize). You can download them again later.") },
            confirmButton = { TextButton(onClick = { confirmClear = false; vm.clearCache() }) { Text("Clear", color = TGBrand.red) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete your account?") },
            text = { Text("This permanently deletes your account, favorites, comments, ratings and search history. It cannot be undone.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.deleteAccount() }) { Text("Delete", color = TGBrand.red) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Sign Out!") },
            text = { Text("Are you sure to logout?") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; vm.signOut() }) { Text("Yes", color = TGBrand.red) } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } },
        )
    }
}

/** Avatar (80dp, white ring), first name in Futura Bold 18, email, or a Sign In button when signed out. */
@Composable
private fun ProfileHeader(name: String, email: String?, avatar: String?, signedIn: Boolean, onSignIn: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
        val ring = Modifier
            .size(TGAccountSize.avatar)
            .shadow(TGSpace.s, CircleShape)
            .border(TGAccountSize.avatarStroke, TGAccountColors.white, CircleShape)
            .clip(CircleShape)
        if (signedIn && !avatar.isNullOrBlank()) {
            TGImage(avatar, contentDescription = "Your profile picture", modifier = ring)
        } else {
            Image(painterResource(R.drawable.avatar), contentDescription = "Default profile picture", contentScale = ContentScale.Crop, modifier = ring)
        }
        Text(name, style = TGText.futuraBold(18), color = TG.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        if (email != null) Text(email, style = TGText.sailecRegular(12), color = TG.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!signedIn) {
            Button(onClick = onSignIn, shape = TGRadius.button, modifier = Modifier.heightIn(min = TGSize.tapMin)) {
                Text("Sign In", style = TGText.sailecBold(16))
            }
        }
    }
}
