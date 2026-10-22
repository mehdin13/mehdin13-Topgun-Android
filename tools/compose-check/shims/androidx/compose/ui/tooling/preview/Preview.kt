package androidx.compose.ui.tooling.preview

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
annotation class Preview(
    val name: String = "",
    val group: String = "",
    val widthDp: Int = -1,
    val heightDp: Int = -1,
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0L,
    val uiMode: Int = 0,
)
