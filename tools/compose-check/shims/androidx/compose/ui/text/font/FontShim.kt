package androidx.compose.ui.text.font

/** Android `Font(resId, weight)`. The JVM check cannot load resources, so it returns the default family's font. */
fun Font(resId: Int, weight: FontWeight = FontWeight.Normal, style: FontStyle = FontStyle.Normal): Font = FontFamily.Default.let {
    object : Font {
        override val weight: FontWeight = weight
        override val style: FontStyle = style
    }
}
