package app.topgun.android.designsystem

import androidx.compose.ui.text.TextStyle

/** iOS large navigation title (`.navigationTitle("Favorite")`, "Settings"): 34 bold. */
val TGText.largeTitle: TextStyle get() = sailecBold(34)

/** iOS `.title` bold (blog detail headline): 28 bold. */
val TGText.detailTitle: TextStyle get() = sailecBold(28)
