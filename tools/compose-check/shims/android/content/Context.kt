package android.content

open class Context {
    open fun getString(resId: Int): String = ""
    open fun getString(resId: Int, vararg formatArgs: Any): String = ""
}
