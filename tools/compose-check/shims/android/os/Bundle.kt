package android.os

class Bundle {
    private val map = mutableMapOf<String, Any?>()
    fun getInt(key: String): Int = (map[key] as? Int) ?: 0
    fun getString(key: String): String? = map[key] as? String
    fun getBoolean(key: String): Boolean = (map[key] as? Boolean) ?: false
    fun putAll(other: Map<String, Any?>) { map.putAll(other) }
}
