package app.topgun.android.features.settings

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import app.topgun.android.data.asString
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
import androidx.compose.material.icons.filled.Apps
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
    /** The support inbox is read in English, so the mail subjects stay English. */
    const val SUPPORT_SUBJECT = "Support email from app user"
    const val FEATURE_SUBJECT = "Feature request"
    /** The Play listing is not published yet: assumes the listing uses the application id. The sentence before it is `R.string.settings_share_message`. */
    const val SHARE_URL = "https://play.google.com/store/apps/details?id=app.topgun.android"
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
    val appIcon by vm.appIcon.collectAsStateWithLifecycle()
    val platform = container.platform
    val context = LocalContext.current
    val language by container.prefs.language.collectAsStateWithLifecycle()
    val shareMessage = stringResource(R.string.settings_share_message)

    var showTheme by rememberSaveable { mutableStateOf(false) }
    var showAppIcon by rememberSaveable { mutableStateOf(false) }
    var showLanguage by rememberSaveable { mutableStateOf(false) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val deleting by vm.deleting.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    BackHandler(enabled = showTheme || showAppIcon || showLanguage) { showTheme = false; showAppIcon = false; showLanguage = false }
    LaunchedEffect(Unit) { vm.refreshCache(); vm.refreshAppIcon() }          // iOS `.onAppear { updateCacheInfo() }`
    LaunchedEffect(toast) {
        toast?.let { snackbar.showSnackbar(it.asString(context)); vm.dismissToast() }
    }

    if (showTheme) {
        ThemeScreen(onBack = { showTheme = false }, onSelect = vm::setAppearance)
        return
    }
    if (showAppIcon) {
        AppIconScreen(selected = appIcon, onBack = { showAppIcon = false }, onSelect = vm::setAppIcon)
        return
    }

    if (showLanguage) {
        LanguageScreen(selected = language, onBack = { showLanguage = false }, onSelect = vm::setLanguage)
        return
    }

    val cacheSize = SettingsViewModel.formatBytes(cache.bytes)
    Box(Modifier.fillMaxSize().background(settingsBackground())) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(bottom = TGSpace.tabBarClearance),
            verticalArrangement = Arrangement.spacedBy(TGSpace.l),
        ) {
            Text(stringResource(R.string.settings_title), style = TGText.largeTitle, color = TG.colors.textPrimary, modifier = Modifier.padding(horizontal = TGSpace.l, vertical = TGSpace.s))
            ProfileHeader(
                name = user.firstName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.settings_default_name),
                email = user.email.takeIf { user.isLoggedIn },
                avatar = user.info?.avatar,
                signedIn = user.isLoggedIn,
                onSignIn = onShowSignIn,
            )

            SettingsSection(stringResource(R.string.settings_section_general)) {
                SettingsRow(
                    Icons.Filled.NotificationsActive, TGAccountColors.systemGreen, stringResource(R.string.settings_notifications),
                    onClick = { vm.setNotifications(!notifications) },
                    trailing = { Switch(checked = notifications, onCheckedChange = vm::setNotifications) },
                )
                SettingsRow(Icons.Filled.Verified, TGBrand.orange, stringResource(R.string.settings_blog), onClick = onOpenBlog, trailing = { SettingsChevron() })
                SettingsRow(
                    Icons.Filled.WorkspacePremium, TGAccountColors.systemYellow, stringResource(R.string.settings_badges), onClick = onOpenBadges, trailing = { SettingsChevron() },
                    showDivider = !user.isPremium,
                )
                if (!user.isPremium) {
                    SettingsRow(
                        Icons.Filled.Verified, TGBrand.blue, stringResource(R.string.settings_get_pro),
                        onClick = { if (user.isLoggedIn) onShowPaywall() else onShowSignIn() },
                    )
                    SettingsRow(
                        Icons.Filled.Restore, TGBrand.blue, stringResource(R.string.settings_restore_purchases), showDivider = false,
                        subtitle = if (restoring) stringResource(R.string.settings_restoring) else null,
                        onClick = { if (user.isLoggedIn) vm.restorePurchases() else onShowSignIn() },
                    )
                }
            }

            SettingsSection(stringResource(R.string.settings_section_preference)) {
                SettingsRow(
                    Icons.Filled.Translate, TGAccountColors.systemPurple, stringResource(R.string.language_title),
                    onClick = { showLanguage = true },
                    trailing = {
                        Text(
                            language?.nativeName ?: stringResource(R.string.language_system_default),
                            style = TGText.sailecRegular(17), color = TG.colors.textSecondary,
                        )
                        SettingsChevron()
                    },
                )
                SettingsRow(
                    Icons.Filled.Inventory2, TGBrand.orange, stringResource(R.string.settings_clear_cache),
                    subtitle = pluralStringResource(R.plurals.settings_cache_summary, cache.fileCount, cache.fileCount, cacheSize),
                    showDivider = false, onClick = { confirmClear = true },
                )
            }

            SettingsSection(stringResource(R.string.settings_section_rate)) {
                SettingsRow(Icons.Filled.Star, TGAccountColors.systemYellow, stringResource(R.string.settings_rate_us), onClick = platform::openRateApp)
                SettingsRow(
                    Icons.Filled.Share, TGAccountColors.systemPurple, stringResource(R.string.settings_share), titleColor = TGBrand.blue, showDivider = false,
                    onClick = { platform.share(shareMessage + "\n" + SettingsLinks.SHARE_URL) },
                )
            }

            SettingsSection(stringResource(R.string.settings_section_support)) {
                SettingsRow(Icons.Filled.Language, TGBrand.blue, stringResource(R.string.settings_website), onClick = { platform.openUrl(SettingsLinks.WEBSITE) })
                SettingsRow(Icons.Filled.Email, TGAccountColors.systemGreen, stringResource(R.string.settings_support), onClick = { platform.sendEmail(SettingsLinks.SUPPORT_EMAIL, SettingsLinks.SUPPORT_SUBJECT) })
                SettingsRow(Icons.Filled.PrivacyTip, TGBrand.red, stringResource(R.string.settings_privacy_policy), onClick = { platform.openUrl(SettingsLinks.PRIVACY) })
                // iOS opens the WishKit feature-request board; there is no Android equivalent, so this writes to support.
                SettingsRow(
                    Icons.Filled.AutoFixHigh, TGBrand.blue, stringResource(R.string.settings_request_feature), showDivider = false,
                    onClick = { platform.sendEmail(SettingsLinks.SUPPORT_EMAIL, SettingsLinks.FEATURE_SUBJECT) },
                )
            }

            SettingsSection(stringResource(R.string.settings_section_theme)) {
                SettingsRow(Icons.Filled.Apps, TGBrand.blue, stringResource(R.string.settings_change_app_icon), onClick = { showAppIcon = true }, trailing = { SettingsChevron() })
                SettingsRow(Icons.Filled.Palette, TGAccountColors.systemYellow, stringResource(R.string.settings_change_theme), showDivider = false, onClick = { showTheme = true }, trailing = { SettingsChevron() })
            }

            SettingsSection(stringResource(R.string.settings_section_account)) {
                SettingsRow(Icons.Filled.WbSunny, TGAccountColors.systemPurple, stringResource(R.string.settings_instagram), onClick = { platform.openUrl(SettingsLinks.INSTAGRAM) })
                SettingsRow(Icons.Filled.Cancel, TG.colors.textPrimary, stringResource(R.string.settings_twitter), showDivider = user.isLoggedIn, onClick = { platform.openUrl(SettingsLinks.TWITTER) })
                if (user.isLoggedIn) {
                    SettingsRow(
                        Icons.AutoMirrored.Filled.Logout, TGBrand.orange, stringResource(R.string.settings_logout), titleColor = TGBrand.red,
                        onClick = { confirmLogout = true },
                    )
                    // Not in the iOS app yet; Google Play requires an in-app way to delete the account.
                    SettingsRow(
                        Icons.Filled.Delete, TGBrand.red, stringResource(R.string.settings_delete_account), titleColor = TGBrand.red, showDivider = false,
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
            title = { Text(stringResource(R.string.settings_clear_dialog_title)) },
            text = { Text(pluralStringResource(R.plurals.settings_clear_dialog_message, cache.fileCount, cache.fileCount, cacheSize)) },
            confirmButton = { TextButton(onClick = { confirmClear = false; vm.clearCache() }) { Text(stringResource(R.string.settings_clear), color = TGBrand.red) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.settings_cancel)) } },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
            text = { Text(stringResource(R.string.settings_delete_dialog_message)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.deleteAccount() }) { Text(stringResource(R.string.settings_delete), color = TGBrand.red) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.settings_cancel)) } },
        )
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(R.string.settings_logout_dialog_title)) },
            text = { Text(stringResource(R.string.settings_logout_dialog_message)) },
            confirmButton = { TextButton(onClick = { confirmLogout = false; vm.signOut() }) { Text(stringResource(R.string.settings_yes), color = TGBrand.red) } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.settings_cancel)) } },
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
            TGImage(avatar, contentDescription = stringResource(R.string.settings_profile_picture), modifier = ring)
        } else {
            Image(painterResource(R.drawable.avatar), contentDescription = stringResource(R.string.settings_default_profile_picture), contentScale = ContentScale.Crop, modifier = ring)
        }
        Text(name, style = TGText.futuraBold(18), color = TG.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        if (email != null) Text(email, style = TGText.sailecRegular(12), color = TG.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!signedIn) {
            Button(onClick = onSignIn, shape = TGRadius.button, modifier = Modifier.heightIn(min = TGSize.tapMin)) {
                Text(stringResource(R.string.settings_sign_in), style = TGText.sailecBold(16))
            }
        }
    }
}
