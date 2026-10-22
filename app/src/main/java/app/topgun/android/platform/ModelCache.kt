// ANDROID-ONLY
package app.topgun.android.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Downloaded 3D models under `cacheDir/models` (iOS `Model3DCache` + `CacheManager`). A model is downloaded once and
 * reused; Settings > Clear Cache calls [clear]. The system may also reclaim `cacheDir` when storage is low.
 */
class ModelCache(private val context: Context) {
    private val dir: File get() = File(context.cacheDir, "models").apply { mkdirs() }

    fun info(): ModelCacheInfo {
        val files = dir.listFiles { f -> f.isFile && !f.name.endsWith(".part") }.orEmpty()
        return ModelCacheInfo(files.size, files.sumOf { it.length() })
    }

    fun clear() {
        dir.listFiles().orEmpty().forEach { it.delete() }
    }

    /** The local file for [url], downloading it first when it is not cached. Null when the download fails. */
    suspend fun fetch(url: String): File? = downloadToCache(dir, url)

    companion object {
        /** Used by the PDF viewer too (it passes its own folder). Returns null on any failure; never throws. */
        suspend fun downloadToCache(folder: File, url: String): File? = withContext(Dispatchers.IO) {
            try {
                folder.mkdirs()
                val ext = url.substringBefore('?').substringAfterLast('.', "bin").take(5)
                val name = MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
                val target = File(folder, "$name.$ext")
                if (target.exists() && target.length() > 0) return@withContext target
                val part = File(folder, "$name.part")
                val conn = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 15_000; readTimeout = 30_000 }
                try {
                    if (conn.responseCode !in 200..299) return@withContext null
                    conn.inputStream.use { input -> part.outputStream().use { input.copyTo(it) } }
                } finally {
                    conn.disconnect()
                }
                if (part.renameTo(target)) target else null
            } catch (e: java.io.IOException) {
                null
            }
        }
    }
}
