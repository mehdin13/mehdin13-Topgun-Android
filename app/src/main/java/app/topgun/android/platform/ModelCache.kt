// ANDROID-ONLY
package app.topgun.android.platform

import app.topgun.android.R
import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Downloaded 3D models under `filesDir/models` (iOS `Model3DCache` + `CacheManager`). One process-wide instance
 * ([get]) so the viewer and the aircraft details screen see the same progress. A model is downloaded once (with real
 * progress from the response stream) and reused by the viewer; the person can also download it ahead for offline use
 * and remove it again. Settings > Clear Cache calls [clear]. The folder is in `filesDir` (not `cacheDir`) so that
 * the system does not reclaim a model the person downloaded on purpose.
 */
class ModelCache private constructor(private val context: Context) : ModelDownloads {
    private val dir: File = File(context.filesDir, "models")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()

    private val lock = Any()
    private val flows = ConcurrentHashMap<String, MutableStateFlow<ModelDownloadState>>()
    private val jobs = HashMap<String, Job>()
    /** URLs the person asked to download; their download is not cancelled when the viewer closes. */
    private val explicit = HashSet<String>()

    init {
        // Models used to be cached in cacheDir/models; they are downloaded again on demand.
        scope.launch { File(context.cacheDir, "models").deleteRecursively() }
    }

    fun info(): ModelCacheInfo {
        val files = dir.listFiles { f -> f.isFile && !f.name.endsWith(".part") }.orEmpty()
        return ModelCacheInfo(files.size, files.sumOf { it.length() })
    }

    fun clear() {
        synchronized(lock) { jobs.values.forEach { it.cancel() } }
        dir.listFiles().orEmpty().forEach { it.delete() }
        flows.values.forEach { it.value = ModelDownloadState.NotDownloaded }
    }

    // ------------------------------------------------------------------ ModelDownloads

    override fun state(url: String): StateFlow<ModelDownloadState> = flow(url)

    override fun download(url: String) = start(url, explicitRequest = true)

    override fun cancel(url: String) {
        synchronized(lock) { jobs[url]?.cancel() }
    }

    override fun remove(url: String) {
        cancel(url)
        targetFor(dir, url).delete()
        flow(url).value = ModelDownloadState.NotDownloaded
    }

    /** Called when a file turned out to be unusable (not a glb): forget it so the next attempt downloads again. */
    fun invalidate(file: File) {
        file.delete()
        flows.forEach { (url, f) -> if (targetFor(dir, url) == file) f.value = ModelDownloadState.NotDownloaded }
    }

    // ------------------------------------------------------------------ viewer API

    /**
     * The local file for [url]: starts the download when needed and waits for it (progress is on [state]).
     * Null when the download fails or is cancelled.
     */
    suspend fun fetch(url: String): File? {
        start(url, explicitRequest = false)
        val done = flow(url).first { it !is ModelDownloadState.Downloading }
        val file = targetFor(dir, url)
        return if (done is ModelDownloadState.Downloaded && file.exists()) file else null
    }

    /** The viewer was closed: stop a download that only the viewer asked for. */
    fun release(url: String) {
        synchronized(lock) { if (url !in explicit) jobs[url]?.cancel() }
    }

    // ------------------------------------------------------------------ internals

    private fun flow(url: String): MutableStateFlow<ModelDownloadState> =
        flows.computeIfAbsent(url) {
            val file = targetFor(dir, url)
            MutableStateFlow(if (file.exists() && file.length() > 0) ModelDownloadState.Downloaded(file.length()) else ModelDownloadState.NotDownloaded)
        }

    private fun start(url: String, explicitRequest: Boolean) {
        synchronized(lock) {
            if (explicitRequest) explicit.add(url)
            val f = flow(url)
            val file = targetFor(dir, url)
            if (f.value is ModelDownloadState.Downloaded && file.exists()) return
            if (jobs[url]?.isActive == true) return
            f.value = ModelDownloadState.Downloading(0L, null)
            val job = scope.launch(start = CoroutineStart.LAZY) { run(url) }
            jobs[url] = job
            job.invokeOnCompletion { synchronized(lock) { if (jobs[url] === job) jobs.remove(url); explicit.remove(url) } }
            job.start()
        }
    }

    /** Streams the response into a `.part` file, publishing bytes read / Content-Length, then renames it into place. */
    private suspend fun run(url: String) {
        val f = flow(url)
        val target = targetFor(dir, url)
        val part = File(dir, target.nameWithoutExtension + ".part")
        try {
            try {
                dir.mkdirs()
                // identity: without gzip the server's Content-Length is kept, which the percentage needs.
                val call = client.newCall(Request.Builder().url(url).header("Accept-Encoding", "identity").build())
                val onCancel = currentCoroutineContext()[Job]?.invokeOnCompletion { if (it != null) call.cancel() }
                try {
                    call.execute().use { response ->
                        val body = response.body
                        if (!response.isSuccessful || body == null) {
                            f.value = ModelDownloadState.Failed(context.getString(R.string.model_err_server, response.code))
                            return
                        }
                        val total = body.contentLength().takeIf { it > 0 }
                        var read = 0L
                        var lastEmit = System.nanoTime() - EMIT_INTERVAL_NANOS
                        f.value = ModelDownloadState.Downloading(0L, total)
                        body.byteStream().use { input ->
                            part.outputStream().use { out ->
                                val buffer = ByteArray(BUFFER_BYTES)
                                while (true) {
                                    currentCoroutineContext().ensureActive()
                                    val n = input.read(buffer)
                                    if (n < 0) break
                                    out.write(buffer, 0, n)
                                    read += n
                                    val now = System.nanoTime()
                                    if (now - lastEmit >= EMIT_INTERVAL_NANOS) {
                                        f.value = ModelDownloadState.Downloading(read, total)
                                        lastEmit = now
                                    }
                                }
                            }
                        }
                        if (total != null && read != total) {
                            part.delete()
                            f.value = ModelDownloadState.Failed(context.getString(R.string.model_err_interrupted))
                            return
                        }
                    }
                } finally {
                    onCancel?.dispose()
                }
                target.delete()
                f.value = if (part.renameTo(target)) ModelDownloadState.Downloaded(target.length()) else ModelDownloadState.Failed(context.getString(R.string.model_err_save))
            } catch (e: IOException) {
                currentCoroutineContext().ensureActive() // an IOException caused by cancelling is a cancellation, not a failure
                part.delete()
                f.value = ModelDownloadState.Failed(e.message ?: context.getString(R.string.model_err_connection))
            }
        } catch (e: CancellationException) {
            part.delete()
            f.value = ModelDownloadState.NotDownloaded
            throw e
        }
    }

    companion object {
        private const val BUFFER_BYTES = 64 * 1024
        private const val EMIT_INTERVAL_NANOS = 100_000_000L

        @Volatile private var instance: ModelCache? = null

        fun get(context: Context): ModelCache =
            instance ?: synchronized(this) { instance ?: ModelCache(context.applicationContext).also { instance = it } }

        private fun targetFor(folder: File, url: String): File {
            val ext = url.substringBefore('?').substringAfterLast('.', "bin").take(5)
            val name = MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
            return File(folder, "$name.$ext")
        }

        /** Used by the PDF viewer too (it passes its own folder). Returns null on any failure; never throws. */
        suspend fun downloadToCache(folder: File, url: String): File? = withContext(Dispatchers.IO) {
            try {
                folder.mkdirs()
                val target = targetFor(folder, url)
                if (target.exists() && target.length() > 0) return@withContext target
                val part = File(folder, target.nameWithoutExtension + ".part")
                val conn = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 15_000; readTimeout = 30_000 }
                try {
                    if (conn.responseCode !in 200..299) return@withContext null
                    conn.inputStream.use { input -> part.outputStream().use { input.copyTo(it) } }
                } finally {
                    conn.disconnect()
                }
                if (part.renameTo(target)) target else null
            } catch (e: IOException) {
                null
            }
        }
    }
}
