package app.topgun.android.features.intro

import app.topgun.android.R

/** One page of the walkthrough (iOS `PageIntro`). [title] may contain a line break, as in iOS. */
data class IntroPage(
    val image: Int,
    val title: String,
    val subtitle: String,
    val displaysAction: Boolean = false,
)

/** Same four pages and copy as iOS `Model/PageIntro.swift` (`pageIntros`). Images `Page 1..4` are `page_1..page_4`. */
val introPages: List<IntroPage> = listOf(
    IntroPage(R.drawable.page_1, "Discover Aircraft", "Explore a world of advanced fighter aircraft, helicopters, and spy planes with Top Gun, the ultimate aviation app. Top Gun offers aviation enthusiasts and professionals in-depth insights into each aircraft’s capabilities, history, and performance, and dive into a world of aviation innovation."),
    IntroPage(R.drawable.page_2, "Aircraft Details", "Dive into the technical specs of each aircraft, from speed and range to armament and radar capabilities and 3d model. Compare performance metrics and learn about the features that make each aircraft unique."),
    IntroPage(R.drawable.page_3, "Favorites and Videos", "Save your favorites, watch high-definition videos, and access exclusive content. Compare performance metrics and learn about the features that make each aircraft unique."),
    IntroPage(R.drawable.page_4, "Let's\nGet Started", "Watch videos and browse galleries for immersive content on your favorite aircraft. Stay updated on the latest aviation news and developments.", displaysAction = true),
)
