package app.topgun.android.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Spacing and radius used across the iOS screens. Android dp = iOS pt. Do not hardcode other values in screens. */
object TGSpace {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    /** Space reserved at the bottom of scrolling screens so content clears the floating tab bar. */
    val tabBarClearance = 150.dp
}

object TGRadius {
    val chip = RoundedCornerShape(6.dp)
    val card = RoundedCornerShape(16.dp)
    val image = RoundedCornerShape(15.dp)
    val button = RoundedCornerShape(12.dp)
    val tabBar = RoundedCornerShape(30.dp)
}

object TGSize {
    /** Minimum touch target. */
    val tapMin = 48.dp
    val listThumbnail = 100.dp
    val gridCardHeight = 200.dp
}

/** Sizes for Favorites, Settings, Blog and Badges (iOS FavoriteView, SettingView, BlogView, BadgeView, BadgeDetailView). Android dp = iOS pt. */
object TGAccountSize {
    /** FavoriteView: `GridItem(.adaptive(minimum: 160), spacing: 15)`. */
    val favoriteGridMinCell = 160.dp
    val gridSpacing = 15.dp
    /** FavoriteView AircraftCard image `.frame(height: 180)`. */
    val favoriteCardImageHeight = 180.dp
    /** FavoriteView AircraftCard name `.frame(height: 40)`. */
    val favoriteCardNameHeight = 40.dp
    /** FavoriteView heart button circle (visual size; the tap area is [TGSize.tapMin]). */
    val heartCircle = 40.dp
    /** FavoriteView `Image(systemName: "magnifyingglass")` in the empty state. */
    val emptyIcon = 50.dp
    /** SettingView profile image `.frame(width: 80, height: 80)`, white stroke 4. */
    val avatar = 80.dp
    val avatarStroke = 4.dp
    /** SettingView row icon (SF Symbol next to the label). */
    val rowIcon = 24.dp
    /** SettingView `Form` row minimum height. */
    val rowMinHeight = 48.dp
    /** BlogView image `minHeight: 200` (iPhone). */
    val blogImageHeight = 200.dp
    /** BlogView source logo in the list (20) and in the detail (30). */
    val blogLogoList = 20.dp
    val blogLogoDetail = 30.dp
    /** BadgeView lock icon `.frame(width: 15, height: 15)`. */
    val badgeLock = 15.dp
    /** BadgeDetailView card (iPhone): 300 x 400, background image 250 x 550, badge image 200 x 200. */
    val badgeCardWidth = 300.dp
    val badgeCardHeight = 400.dp
    val badgeCardBackWidth = 250.dp
    val badgeCardBackHeight = 550.dp
    val badgeCardImage = 200.dp
    val badgeCardShadow = 20.dp
    /** ChangeIconView preview `.frame(width: 60, height: 60)` with `.clipShape(.rect(cornerRadius: 10))` (see [TGAccountRadius.blogListImage]). */
    val iconPreview = 60.dp
}

object TGAccountRadius {
    /** BlogView list image `.cornerRadius(10)`. */
    val blogListImage = RoundedCornerShape(10.dp)
    /** BadgeDetailView `RoundedRectangle(cornerRadius: 30)`. */
    val badgeCard = RoundedCornerShape(30.dp)
}

/** Home screen and filter sheet spacing (iOS HomeView.swift, FilterView.swift). */
object TGHomeSpace {
    /** `VStack(spacing: 15)` between the Home sections and `HStack(spacing: 15)` between feature cards. */
    val section = 15.dp
    /** `HeaderSection .padding(.top, 20)` and the Welcome `VStack(spacing: 20)`. */
    val headerTop = 20.dp
    /** `CategoriesSection` title `.padding(.top, 25)`. */
    val categoryTop = 25.dp
    /** `Text("Aircraft List") .padding(.top, 24)`. */
    val listTitleTop = 24.dp
    /** Category tile `VStack` `.padding(5)`. */
    val categoryTile = 5.dp
    /** Chip row spacing in the list card is in the shared card; this is the search bar inner padding (12 vertical, 15 horizontal). */
    val searchVertical = 12.dp
    val searchHorizontal = 15.dp
    /** Filter sheet row vertical padding (iOS `List` row) and section gap. */
    val filterRow = 10.dp
}

object TGHomeSize {
    /** Header logo `.frame(width: 172, height: 70)`. */
    val logoWidth = 172.dp
    val logoHeight = 70.dp
    /** FeaturedSectionCardView `.frame(width: 220, height: 120)`. */
    val featureCardWidth = 220.dp
    val featureCardHeight = 120.dp
    /** Feature card icon (`.title3` ~ 20) inside an 8pt padded circle. */
    val featureIcon = 20.dp
    val featureIconPadding = 8.dp
    /** CategoryItem tile 60x60 (iPhone), image 40x40, selected underline height 2. */
    val categoryTile = 60.dp
    val categoryImage = 40.dp
    val categoryUnderline = 2.dp
    /** Scroll distance before the tab bar hides / shows (iOS ScrollOffsetPreferenceKey threshold 10). */
    val tabBarScrollThreshold = 10.dp
    /** Filter sheet: flag 30x20, radio 20 with 14 dot (stroke 2), crew icon 40. */
    val flagWidth = 30.dp
    val flagHeight = 20.dp
    val radio = 20.dp
    val radioDot = 14.dp
    val radioStroke = 2.dp
    val crewIcon = 40.dp
    val sectionIcon = 20.dp
}

object TGHomeRadius {
    /** Flag `.clipShape(RoundedRectangle(cornerRadius: 3))`. */
    val flag = RoundedCornerShape(3.dp)
}

/**
 * Sizes, timings and text styles of the entry flow (Intro, Welcome story, Splash, Sign-in).
 * iOS sources: `View/Intro/Intro.swift`, `CustomIndicatorView.swift`, `View/WelcomeAppStory/WelcomeAppStory.swift`,
 * `View/MainViews/SplashView.swift`, `View/MainViews/SignInView.swift`. Android dp = iOS pt.
 */
object TGEntry {
    /** Intro `.padding(15)` around the page and around the image. */
    val pagePadding = 15.dp
    /** Intro "Next" / "Continue" capsule: `.padding(.vertical, 15)`. */
    val buttonVerticalPadding = 15.dp
    /** Intro "Next" button width: `size.width * 0.4`. */
    const val nextButtonWidthFraction = 0.4f
    /** CustomIndicatorView: 4 pt circles, 8 pt apart. */
    val indicatorDot = 4.dp
    val indicatorSpacing = 8.dp
    /** Intro back chevron slides in from 200 pt above. */
    val backButtonTravel = 200.dp

    /** Splash `LottieView(...).frame(width: 150, height: 150)`. */
    val splashLogo = 150.dp

    /** SignInView: logo 250 x 250, `.padding(.top, 50)`. */
    val signInLogo = 250.dp
    val signInLogoTop = 50.dp
    /** SignInView description: `.padding(.top, 20)`, `.padding(.horizontal, 40)`. */
    val signInTextTop = 20.dp
    val signInTextHorizontal = 40.dp
    /** SignInView links row: `.padding(.bottom, 40)`. */
    val signInBottom = 40.dp
    /** SocialLoginButton: height 50, `.cornerRadius(30)`, shadow (radius 60, y 16, 8 % black) approximated by an elevation. */
    val socialButtonHeight = 50.dp
    val socialButtonRadius = 30.dp
    val socialButtonShadow = 8.dp
    /** Google "G" in the social button (iOS uses the image's natural size; assumption). */
    val socialIcon = 24.dp

    /** WelcomeAppStory: image height 210, subtitle width 300, top progress bars 6 pt high with radius 3. */
    val welcomeImageHeight = 210.dp
    val welcomeSubtitleWidth = 300.dp
    val welcomeBarHeight = 6.dp
    val welcomeBarRadius = 3.dp
    val welcomeSubtitleBottom = 60.dp

    // Motion. iOS `.spring(response: 0.8, dampingFraction: 0.8)`: stiffness = (2*pi/response)^2 ~= 62.
    const val springDamping = 0.8f
    const val springStiffness = 62f
    /** Intro: `.delay(0.1)` before the first reveal, 0.5 s before swapping to the next page. */
    const val introEnterDelayMs = 100L
    const val introSwapDelayMs = 500L
    /** Splash shows for 3 s (`asyncAfter(.now() + 3)`). */
    const val splashDurationMs = 3000L
    /** WelcomeAppStory `Timer.publish(every: 3)`. */
    const val welcomeAutoAdvanceMs = 3000L

    /** Intro subtitle: `FontHelper.customFont(FontHelper.FuturaLight, size: 15)`. */
    val subtitleStyle: androidx.compose.ui.text.TextStyle
        get() = androidx.compose.ui.text.TextStyle(
            fontFamily = TGFonts.futura,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
            fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp),
        )
    /** Intro title: `.font(.system(size: 30)).fontWeight(.black)`. */
    val titleStyle: androidx.compose.ui.text.TextStyle get() = TGText.rounded(30)
    /** SignInView description (`.subheadline`, 15 pt) and links (`.footnote`, 13 pt). */
    val signInBodyStyle: androidx.compose.ui.text.TextStyle get() = TGText.sailecRegular(15)
    val footnoteStyle: androidx.compose.ui.text.TextStyle get() = TGText.sailecRegular(13)
    /** SocialLoginButton label: `.title3` (20 pt). */
    val socialLabelStyle: androidx.compose.ui.text.TextStyle get() = TGText.sailecMedium(20)
    /** Capsule button label: `.fontWeight(.semibold)`. */
    val capsuleLabelStyle: androidx.compose.ui.text.TextStyle get() = TGText.sailecBold(16)
    /** WelcomeAppStory title (24 bold) and subtitle (15 regular). */
    val welcomeTitleStyle: androidx.compose.ui.text.TextStyle get() = TGText.rounded(24, androidx.compose.ui.text.font.FontWeight.Bold)
    val welcomeSubtitleStyle: androidx.compose.ui.text.TextStyle get() = TGText.sailecRegular(15)
}

/**
 * Sizes for countries, manufacturers, curated lists and the map/globe.
 * iOS sources: CountryView.swift, ManufacturerView.swift, ManufacturerListView.swift, FeaturedCardItem.swift,
 * CustomAircraftGridView.swift, GlassmorphicCalloutView.swift, MapView.swift, GlobeView.swift, MapAnnotationView.swift.
 */
object TGGeo {
    /** Cover banner of country and manufacturer detail (iPhone value). */
    val coverHeight = 200.dp
    /** Round flag / logo that overlaps the bottom of the cover. */
    val coverLogo = 80.dp
    val coverLogoBorder = 2.dp
    /** Space under the cover for the overlapping logo (CountryView `.padding(.top, 50)`). */
    val detailTopInset = 50.dp
    val detailHorizontal = 24.dp
    val equipmentCardWidth = 160.dp
    val equipmentImageHeight = 120.dp
    val equipmentNameHeight = 36.dp
    /** Height of the 2-row horizontal equipment grid: country detail 400, manufacturer detail 380. */
    val countryEquipmentHeight = 400.dp
    val manufacturerEquipmentHeight = 380.dp
    /** ManufacturerWideCard. */
    val wideCardWidth = 320.dp
    val wideCardHeight = 140.dp
    val wideCardLogo = 50.dp
    /** FeaturedCardItem (country grid): flag size and 2-line name box. */
    val featuredFlagWidth = 50.dp
    val featuredFlagHeight = 40.dp
    val featuredNameHeight = 32.dp
    val featuredColumns = 3
    /** ManufacturerCard (manufacturer list). */
    val manufacturerCover = 140.dp
    val manufacturerBadge = 60.dp
    val manufacturerFlagWidth = 18.dp
    val manufacturerFlagHeight = 12.dp
    /** CustomListAircraftCard (curated list grid). */
    val gridMinCell = 110.dp
    val gridThumbHeight = 120.dp
    val gridNameHeight = 32.dp
    val gridRoleHeight = 30.dp
    /** Callout shown on a tapped map pin (GlassmorphicCalloutView). */
    val calloutWidth = 280.dp
    /** Diameter of a flag pin on the globe (MapAnnotationView uses 30 inside a 34 ring). */
    val pin = 30.dp
    val pinRing = 34.dp
    /** Round overlay buttons (back, map/globe toggle). */
    val overlayButton = 40.dp
    val globeSize = 300.dp

    val shape20 = RoundedCornerShape(20.dp)   // FeaturedCardItem, callout
    val shape12 = RoundedCornerShape(12.dp)   // equipment cards, grid cards
    val shape10 = RoundedCornerShape(10.dp)   // grid thumbnail

    /** Opacities used on images and glass panels. */
    const val scrimLight = 0.3f
    const val scrim = 0.6f
    const val glass = 0.2f
    const val tint = 0.12f
}

/** Aircraft detail screen sizes. iOS: `DetailsView.swift`, `DetailsView+Components.swift`, `GalleryView.swift`, `CommentsListView.swift` (pt = dp). */
object TGDetailSize {
    /** `AircraftHeaderView` image height (iPhone). */
    val headerHeight = 400.dp
    /** `CircleButtonStyle` 40x40 (the touch target is [TGSize.tapMin]). */
    val circleButton = 40.dp
    /** `AllSettingsItem` height (iPhone). */
    val tileHeight = 70.dp
    val tileIcon = 44.dp
    val tileRadius = RoundedCornerShape(10.dp)
    /** `DetailsInfoView` corner radius 10 and hairline 0.25. */
    val cellRadius = RoundedCornerShape(10.dp)
    val hairlineWidth = 0.5.dp
    /** `VideoSection` / `InsetMapView` height (iPhone). */
    val mediaHeight = 256.dp
    val mediaRadius = RoundedCornerShape(12.dp)
    val pill = RoundedCornerShape(12.dp)
    /** `CardDeckExample`: portrait card aspect 0.7, corner 20. */
    const val deckAspect = 0.7f
    val deckRadius = RoundedCornerShape(20.dp)
    val deckPeek = 40.dp
    /** `CountryFlagIcon` frame. */
    val flag = 60.dp
    val flagGap = 12.dp
    /** `ManufacturerInfoView`: 35 logo + 10 padding. */
    val logo = 35.dp
    val logoPadding = 10.dp
    /** `GalleryView` bottom thumbnails 70x60. */
    val thumbWidth = 70.dp
    val thumbHeight = 60.dp
    val thumbRadius = RoundedCornerShape(12.dp)
    val sketchHeight = 250.dp
    /** `DetailsView.ratingSection` star size 30; `StarRatingView(size: 10)` in the comments header. */
    val ratingStar = 30.dp
    val smallStar = 12.dp
    /** `CommentsListView`: avatars 40 / 36, input corner 20. */
    val avatar = 40.dp
    val inputAvatar = 36.dp
    val sendButton = 36.dp
    val inputRadius = RoundedCornerShape(20.dp)
    /** Single line of a spec row icon column (`SettingItem` icon width 24). */
    val specIcon = 24.dp
    val modelHeight = 350.dp
    val modelButtonWidth = 250.dp
    val buttonRadius = RoundedCornerShape(20.dp)
    /** Android-only: Instagram-style sliding page dots (iOS `CustomIndicatorView` shows every dot; with many photos that overflows). */
    val dotSize = 8.dp
    val dotGap = 6.dp
    const val dotWindow = 5
    const val dotSelectedScale = 1.4f
    const val dotEdgeScale = 0.5f
    /** Android-only: play button over the YouTube thumbnail (iOS uses the system YouTube player). */
    val playButton = 64.dp
}

/** Odd paddings used by the iOS detail screen that the shared [TGSpace] scale does not have. */
object TGDetailSpace {
    val zero = 0.dp
    val hair = 2.dp
    val xxs = 3.dp
    val five = 5.dp
    val six = 6.dp
    val ten = 10.dp
    val pillH = 12.dp
    val pillV = 6.dp
    /** Top inset of the header buttons below the status bar (iOS 46 includes the status bar; this is added to it). */
    val headerButtonTop = 8.dp
    val headerButtonSide = 24.dp
    val bottomClear = 150.dp
}
