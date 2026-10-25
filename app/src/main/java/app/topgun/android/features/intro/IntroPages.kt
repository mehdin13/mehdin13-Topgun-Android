package app.topgun.android.features.intro

import app.topgun.android.R

/** One page of the walkthrough (iOS `PageIntro`). [title] and [subtitle] are `R.string` ids; the title may contain a line break, as in iOS. */
data class IntroPage(
    val image: Int,
    val title: Int,
    val subtitle: Int,
    val displaysAction: Boolean = false,
)

/** Same four pages and copy as iOS `Model/PageIntro.swift` (`pageIntros`). Images `Page 1..4` are `page_1..page_4`. */
val introPages: List<IntroPage> = listOf(
    IntroPage(R.drawable.page_1, R.string.intro_page1_title, R.string.intro_page1_subtitle),
    IntroPage(R.drawable.page_2, R.string.intro_page2_title, R.string.intro_page2_subtitle),
    IntroPage(R.drawable.page_3, R.string.intro_page3_title, R.string.intro_page3_subtitle),
    IntroPage(R.drawable.page_4, R.string.intro_page4_title, R.string.intro_page4_subtitle, displaysAction = true),
)
