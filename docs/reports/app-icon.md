# App icon (launcher) and Settings > Change App icon

## What was done
- Artwork: the 1024px iOS icons from `Assets.xcassets/AppIcons` (`AppIcon` = Default, `AppIcon1..4`). Icon names in the picker are the iOS ones (Default, AppIcon1..4).
- Adaptive icons (`res/mipmap-anydpi-v26/ic_launcher*.xml`, `ic_launcher_alt1..4*.xml`, plus `_round`): foreground = full artwork at 70% on a 432px transparent canvas (`mipmap-nodpi/*_fg.png`), background = heavily blurred copy of the artwork (`*_bg.png`). The visible mask (72dp of 108dp) shows the whole artwork with no seam. No monochrome layer (artwork is photographic; themed icons would look wrong).
- Legacy PNGs `ic_launcher.png` / `_round.png` for mdpi..xxxhdpi (unused at minSdk 26, harmless).
- Manifest: `android:icon`/`roundIcon` on `<application>`; `MainActivity` has no launcher filter; five `<activity-alias>` (`.IconDefault` enabled, `.IconAlt1..4` disabled) with MAIN/LAUNCHER and their own icons.
- `PlatformServices.setAppIcon(index)` / `currentAppIcon()`; `AndroidPlatform` toggles the aliases (enable chosen first, then disable the others, DONT_KILL_APP); `FakePlatform` and `HomeFixtures` updated.
- UI: `features/settings/AppIconScreen.kt` (new), "Change App icon" row in the Theme section (above Change Theme), `SettingsViewModel.appIcon/setAppIcon/refreshAppIcon`. Previews are `res/drawable-nodpi/icon_preview_0..4.png` (256px). Token added: `TGAccountSize.iconPreview` (60dp, iOS ChangeIconView).

## How to test
Install the debug APK, Settings > Theme > Change App icon, tap an icon. The launcher icon should change within a few seconds (some launchers restart or close the app). Check the check mark persists after reopening the screen. Long-press the home icon / check the app drawer shows only ONE Top Gun entry.

## Verification
- `tools/compose-check/check.sh /home/user/scratch/cc-icon`: `compose-check: OK`.
- All new XML and the manifest parse with minidom; composited the adaptive layers with ImageMagick and viewed them (circle mask) for 3 icons.

## Not verified
- Nothing was built with AGP/aapt (resource linking, alias names) or run on a device. `AndroidPlatform.kt` is ANDROID-ONLY and not type-checked.
- Behavior differs by launcher (delay, app being killed on some). Existing installs that had the old default icon will have the default alias enabled by the manifest.
- Icon look in real launcher shapes (squircle, teardrop) is not seen; only a circle mask was simulated.
- Anything that launched `MainActivity` by explicit component keeps working; launcher shortcuts/pinned icons of a disabled alias disappear when the user switches (Android behavior).
