// ANDROID-ONLY
package app.topgun.android.platform.ui

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.key
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.ModelDownloadProgress
import app.topgun.android.platform.ModelDownloadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.dp
import app.topgun.android.platform.ModelCache
import com.google.android.filament.Engine
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import io.github.sceneview.Scene
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.SceneView
import io.github.sceneview.environment.Environment
import io.github.sceneview.loaders.EnvironmentLoader
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.model.Model
import io.github.sceneview.node.Node
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader
import app.topgun.android.platform.formatByteCount
import java.io.FileInputStream
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import dev.romainguy.kotlin.math.Float3
import java.io.File
import java.nio.ByteBuffer

/**
 * SceneView (Filament) viewer and ARCore placement for glb / gltf models. The caller only passes a glb/gltf URL
 * (the backend's .usdz cannot be loaded here). The file is downloaded once into [ModelCache].
 */
internal object ModelUi {
    private sealed interface Fetch {
        /** [progress] is the live download state (bytes read / Content-Length); null before the first update. */
        data class Loading(val progress: ModelDownloadState.Downloading?) : Fetch
        class Failed(val message: String?, val retry: () -> Unit) : Fetch
        /** Not on the device and not downloading (cancelled from the 3D page card, or removed): wait for the person. */
        class Idle(val start: () -> Unit) : Fetch
        data class Ready(val file: File) : Fetch
    }

    /** The downloaded file is not a glb/gltf (for example an HTML error page). */
    private class BadModelFile : IOException("The downloaded file is not a glb model")

    /** Stage names and timings of the viewer, shown under it (see DiagnosticCaption). Main thread only. */
    private class ViewerTrace(private val context: Context) {
        private var readyAtNanos = System.nanoTime()
        private var firstFrameSeen = false
        var parts: List<String> by mutableStateOf(emptyList())
            private set

        fun add(text: String) { parts = parts + text }

        /** The file is on the device: timings below are relative to this moment. */
        fun fileReady() { readyAtNanos = System.nanoTime() }

        fun firstFrame() {
            if (firstFrameSeen) return
            firstFrameSeen = true
            add(context.getString(R.string.m3n_diag_first_frame, ((System.nanoTime() - readyAtNanos) / 1_000_000L).toInt()))
        }

        fun error(e: Throwable) {
            add(context.getString(R.string.m3n_diag_error, e::class.java.name.substringAfterLast('.'), e.message ?: "-"))
        }
    }

    private sealed interface Loaded {
        data object Loading : Loaded
        data class Failed(val reason: String, val badFile: Boolean) : Loaded
        /** [buffer] is kept referenced: Filament reads the glb in place and does not copy it. */
        class Ready(val model: Model, val instance: ModelInstance, val buffer: ByteBuffer) : Loaded
    }

    /**
     * Derives the viewer state from the shared [ModelCache] state flow (single source of truth), so a download started
     * here or from the offline card, finishing while the viewer is visible, shows the model at once: the state turns
     * Downloaded and the file is handed on. Leaving the screen stops a download that only the viewer started.
     */
    @Composable
    private fun rememberModelFile(url: String): Fetch {
        val cache = ModelCache.get(LocalContext.current)
        var attempt by remember(url) { mutableIntStateOf(0) }
        var requested by remember(url) { mutableStateOf(false) }
        val state by remember(url) { cache.state(url) }.collectAsState()
        val downloadedFile = remember(state) { if (state is ModelDownloadState.Downloaded) cache.fileIfPresent(url) else null }
        LaunchedEffect(url, attempt) {
            cache.request(url)
            requested = true
        }
        DisposableEffect(url) { onDispose { cache.release(url) } }
        return when (val s = state) {
            is ModelDownloadState.Downloaded -> downloadedFile?.let { Fetch.Ready(it) } ?: Fetch.Loading(null)
            is ModelDownloadState.Downloading -> Fetch.Loading(s)
            is ModelDownloadState.Failed -> Fetch.Failed(s.message) { attempt++ }
            ModelDownloadState.NotDownloaded -> if (requested) Fetch.Idle { attempt++ } else Fetch.Loading(null)
        }
    }

    /** Download progress with percentage, or an indeterminate spinner when no download is running (e.g. reading the file). */
    @Composable
    private fun LoadingModel(progress: ModelDownloadState.Downloading?, modifier: Modifier = Modifier) {
        if (progress != null) {
            ModelDownloadProgress(progress, modifier.padding(horizontal = TGSpace.xl))
        } else {
            Column(modifier.padding(TGSpace.l), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                CircularProgressIndicator(color = TG.colors.accent)
                Text(stringResource(R.string.model_preparing), style = TGText.sailecMedium(14), color = TG.colors.textSecondary)
            }
        }
    }

    @Composable
    private fun DownloadFailed(fetch: Fetch.Failed, modifier: Modifier = Modifier) {
        Column(modifier.padding(TGSpace.l), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TGSpace.m)) {
            ErrorText(stringResource(R.string.model_download_error) + (fetch.message?.let { " ($it)" } ?: ""))
            Button(onClick = fetch.retry, modifier = Modifier.defaultMinSize(minHeight = TGSize.tapMin)) { Text(stringResource(R.string.ds_try_again), style = TGText.sailecMedium(16)) }
        }
    }

    @Composable
    private fun IdleModel(fetch: Fetch.Idle, modifier: Modifier = Modifier) {
        Column(modifier.padding(TGSpace.l), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = fetch.start, modifier = Modifier.defaultMinSize(minHeight = TGSize.tapMin)) { Text(stringResource(R.string.model_download), style = TGText.sailecMedium(16)) }
        }
    }

    /**
     * Reads the file into the direct buffer Filament needs, off the main thread and without a heap copy (a 25 MB model
     * used to cost a 25 MB ByteArray plus a 25 MB direct copy). Throws when the file is not a glb/gltf (e.g. an HTML error page).
     */
    private suspend fun readModelBuffer(file: File): ByteBuffer = withContext(Dispatchers.IO) {
        FileInputStream(file).channel.use { channel ->
            val size = channel.size()
            if (size < MIN_MODEL_BYTES) throw BadModelFile()
            if (size > Int.MAX_VALUE) throw IOException("The model file is too large")
            val buffer = ByteBuffer.allocateDirect(size.toInt())
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) < 0) break
            }
            if (buffer.hasRemaining()) throw IOException("The model file ended early")
            buffer.flip()
            val isGlb = buffer.get(0) == 'g'.code.toByte() && buffer.get(1) == 'l'.code.toByte() && buffer.get(2) == 'T'.code.toByte() && buffer.get(3) == 'F'.code.toByte()
            val isGltfJson = file.name.endsWith(".gltf", ignoreCase = true) && buffer.get(0) == '{'.code.toByte()
            if (!isGlb && !isGltfJson) throw BadModelFile()
            buffer
        }
    }

    private fun elapsedMs(sinceNanos: Long): Int = ((System.nanoTime() - sinceNanos) / 1_000_000L).toInt()

    /**
     * Builds the Filament asset for [file]. Runs on the main thread (Filament objects belong to the thread that created
     * the Engine) but is split so that nothing long blocks it: the file is read on IO, `createAsset` parses and uploads the
     * geometry, and the textures are decoded on Filament's own worker threads (`asyncBeginLoad`) while this coroutine
     * pumps `asyncUpdateLoad()` once per frame, so the "Preparing 3D model" state keeps animating. SceneView's own
     * `createModelInstance` calls the synchronous `loadResources`, which decodes every JPEG/PNG on the main thread
     * (freezing the UI for as long as that takes) and is the call this replaces. The asset is not registered with
     * [ModelLoader] (its list is private), so the caller destroys it with [ModelLoader.destroyModel].
     */
    private suspend fun prepareModel(loader: ModelLoader, file: File, context: Context, trace: ViewerTrace?, badFile: String, unknownError: String): Loaded {
        var created: Model? = null
        var handedOff = false
        try {
            var t = System.nanoTime()
            val buffer = readModelBuffer(file)
            trace?.add(context.getString(R.string.m3n_diag_read, elapsedMs(t)))
            t = System.nanoTime()
            val model = loader.assetLoader.createAsset(buffer) ?: throw IllegalStateException("Filament could not parse this model")
            created = model
            // An already loaded asset answers false; there is nothing left to wait for then.
            loader.resourceLoader.asyncBeginLoad(model)
            trace?.add(context.getString(R.string.m3n_diag_parsed, elapsedMs(t)))
            t = System.nanoTime()
            var timedOut = false
            while (true) {
                loader.resourceLoader.asyncUpdateLoad()
                if (loader.resourceLoader.asyncGetLoadProgress() >= 1f) break
                if (elapsedMs(t) > TEXTURE_TIMEOUT_MS) {
                    timedOut = true // show the model anyway; the Scene keeps pumping asyncUpdateLoad() every frame
                    break
                }
                withFrameNanos { }
            }
            trace?.add(context.getString(if (timedOut) R.string.m3n_diag_textures_timeout else R.string.m3n_diag_textures, elapsedMs(t)))
            val instance = model.instance ?: throw IllegalStateException("The model has no instance")
            handedOff = true
            return Loaded.Ready(model, instance, buffer)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) { // Filament throws NPE / IllegalStateException for broken assets; OutOfMemoryError for huge ones
            trace?.error(e)
            return Loaded.Failed(if (e is BadModelFile) badFile else e.message ?: unknownError, e is BadModelFile)
        } finally {
            if (!handedOff) {
                created?.let { model ->
                    runCatching { loader.resourceLoader.asyncCancelLoad() }
                    runCatching { loader.destroyModel(model) }
                }
            }
        }
    }

    /** A model that fails to parse is reported as [Loaded.Failed] instead of crashing. */
    @Composable
    private fun rememberModelInstance(loader: ModelLoader, file: File, reload: Int = 0, trace: ViewerTrace? = null): Loaded {
        val badFile = stringResource(R.string.model_bad_file)
        val unknownError = stringResource(R.string.model_unknown_error)
        val appContext = LocalContext.current.applicationContext
        val state = produceState<Loaded>(Loaded.Loading, loader, file, reload) {
            value = prepareModel(loader, file, appContext, trace, badFile, unknownError)
        }
        val ready = state.value as? Loaded.Ready
        // Declared before (so disposed after) the nodes and the Scene that use the asset, and before the loader/engine.
        DisposableEffect(ready) {
            onDispose { ready?.let { runCatching { loader.destroyModel(it.model) } } }
        }
        return state.value
    }

    @Composable
    private fun ErrorText(text: String, modifier: Modifier = Modifier) =
        Text(text, style = TGText.sailecMedium(14), color = TG.colors.textSecondary, textAlign = TextAlign.Center, modifier = modifier.padding(TGSpace.l))

    /** Waits two frames so the page is on screen before the Engine (EGL context, shader archive, environment) is built. */
    @Composable
    private fun rememberAfterFirstFrames(): Boolean {
        var ready by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            withFrameNanos { }
            ready = true
        }
        return ready
    }

    /** Stage name and timings, always visible under the viewer so a failure can be reported with a screenshot. */
    @Composable
    private fun DiagnosticCaption(fetch: Fetch, trace: ViewerTrace, modifier: Modifier = Modifier) {
        val text = when (fetch) {
            is Fetch.Loading -> fetch.progress?.let { p ->
                p.totalBytes?.let { stringResource(R.string.m3n_diag_downloading, formatByteCount(p.bytesRead), formatByteCount(it)) }
                    ?: stringResource(R.string.m3n_diag_downloading_unknown, formatByteCount(p.bytesRead))
            } ?: stringResource(R.string.m3n_diag_waiting)
            is Fetch.Failed -> stringResource(R.string.m3n_diag_failed, fetch.message ?: "-")
            is Fetch.Idle -> stringResource(R.string.m3n_diag_idle)
            is Fetch.Ready -> {
                val size = remember(fetch.file) { formatByteCount(fetch.file.length()) }
                (listOf(stringResource(R.string.m3n_diag_downloaded, size)) + trace.parts).joinToString(" → ")
            }
        }
        Text(text, style = TGText.sailecRegular(11), color = TG.colors.textSecondary, modifier = modifier.fillMaxWidth().padding(top = TGSpace.xs))
    }

    @Composable
    fun Viewer(modelUrl: String, modifier: Modifier) {
        val context = LocalContext.current
        val trace = remember(modelUrl) { ViewerTrace(context.applicationContext) }
        val fetch = rememberModelFile(modelUrl)
        val engineWanted = rememberAfterFirstFrames()
        Column(Modifier.fillMaxWidth()) {
            Box(modifier.background(TG.colors.card), contentAlignment = Alignment.Center) {
                if (engineWanted) ViewerStage(modelUrl, fetch, trace) else Placeholder(fetch)
            }
            DiagnosticCaption(fetch, trace)
        }
    }

    /** Download progress, failure, idle or preparing: everything that is shown instead of the 3D scene. */
    @Composable
    private fun Placeholder(fetch: Fetch) {
        when (fetch) {
            is Fetch.Loading -> LoadingModel(fetch.progress)
            is Fetch.Failed -> DownloadFailed(fetch)
            is Fetch.Idle -> IdleModel(fetch)
            is Fetch.Ready -> LoadingModel(null)
        }
    }

    /**
     * Builds the Engine and everything the Scene needs that does not depend on the model (loaders, environment/IBL)
     * as soon as the page is up, while the download is still running, so none of that work is left for the moment the
     * download completes. The model itself is prepared by [ReadyModel] once the file is on the device.
     */
    @Composable
    private fun ViewerStage(modelUrl: String, fetch: Fetch, trace: ViewerTrace) {
        val context = LocalContext.current
        val startedAt = remember { System.nanoTime() }
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val materialLoader = rememberMaterialLoader(engine)
        val environmentLoader = rememberEnvironmentLoader(engine)
        val environment = rememberEnvironment(environmentLoader)
        val warmMs = remember { elapsedMs(startedAt) }
        LaunchedEffect(Unit) { trace.add(context.applicationContext.getString(R.string.m3n_diag_engine, warmMs)) }
        when (fetch) {
            is Fetch.Ready -> key(fetch.file.path) {
                ReadyModel(modelUrl, fetch.file, engine, modelLoader, materialLoader, environmentLoader, environment, trace)
            }
            else -> Placeholder(fetch)
        }
    }

    @Composable
    private fun ReadyModel(
        modelUrl: String,
        file: File,
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        environmentLoader: EnvironmentLoader,
        environment: Environment,
        trace: ViewerTrace,
    ) {
        val appContext = LocalContext.current.applicationContext
        val cache = ModelCache.get(appContext)
        remember { trace.fileReady() }
        // The manipulator rebuilds the camera every frame from its own home position, so the home position
        // (not only cameraNode.position) must be set or the camera starts inside the model. Built with plain remember
        // (not rememberCameraNode / rememberCameraManipulator, whose creator lambdas capture a new Float3 on every
        // recomposition and would replace the camera and the manipulator each time).
        val home = remember { Float3(0f, 0f, CAMERA_DISTANCE) }
        val cameraNode = remember(engine) { SceneView.createCameraNode(engine).apply { position = home } }
        DisposableEffect(cameraNode) { onDispose { runCatching { cameraNode.destroy() } } }
        val manipulator = remember { SceneView.createDefaultCameraManipulator(home, Float3(0f, 0f, 0f)) }
        var reload by remember { mutableIntStateOf(0) }
        when (val loaded = rememberModelInstance(modelLoader, file, reload, trace)) {
            Loaded.Loading -> LoadingModel(null)
            is Loaded.Failed -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TGSpace.m)) {
                ErrorText(stringResource(R.string.model_display_failed, loaded.reason))
                Button(
                    onClick = {
                        if (loaded.badFile) { // do not keep a bad download: fetch it again
                            cache.invalidate(file)
                            cache.request(modelUrl)
                        } else {
                            reload++
                        }
                    },
                    modifier = Modifier.defaultMinSize(minHeight = TGSize.tapMin),
                ) { Text(stringResource(R.string.ds_try_again), style = TGText.sailecMedium(16)) }
            }
            is Loaded.Ready -> {
                // Keyed by the instance (not a one-shot rememberNodes lambda): a new instance always gets a new node.
                val built = remember(loaded.instance) {
                    val t = System.nanoTime()
                    val result = runCatching {
                        ModelNode(modelInstance = loaded.instance, scaleToUnits = MODEL_UNITS, autoAnimate = true).apply {
                            // Centre the model on the orbit target: scale about the node origin, then shift by the scaled bbox centre.
                            val k = scale.x
                            position = Float3(-center.x * k, -center.y * k, -center.z * k)
                        }
                    }
                    result to elapsedMs(t)
                }
                val node = built.first.getOrNull()
                LaunchedEffect(built) {
                    val failure = built.first.exceptionOrNull()
                    if (failure != null) trace.error(failure) else trace.add(appContext.getString(R.string.m3n_diag_node, built.second))
                }
                if (node == null) {
                    ErrorText(stringResource(R.string.model_display_failed, built.first.exceptionOrNull()?.message ?: stringResource(R.string.model_unknown_error)))
                } else {
                    DisposableEffect(node) { onDispose { runCatching { node.destroy() } } }
                    val nodes = remember(node) { listOf<Node>(node) }
                    Scene(
                        modifier = Modifier.fillMaxSize(),
                        engine = engine,
                        modelLoader = modelLoader,
                        materialLoader = materialLoader,
                        environmentLoader = environmentLoader,
                        environment = environment,
                        cameraNode = cameraNode,
                        cameraManipulator = manipulator,
                        childNodes = nodes,
                        // Finishes texture uploads when the wait in prepareModel timed out (cheap when nothing is pending).
                        onFrame = { _ ->
                            modelLoader.resourceLoader.asyncUpdateLoad()
                            trace.firstFrame()
                        },
                        onViewCreated = { trace.add(appContext.getString(R.string.m3n_diag_rendering)) },
                    )
                }
            }
        }
    }

    /** Fits the longest side of the aircraft into this many Filament units (metres). */
    private const val MODEL_UNITS = 1f
    private const val CAMERA_DISTANCE = 2.2f
    private const val MIN_MODEL_BYTES = 13L

    /** Longest wait for the worker threads to decode the textures before the model is shown with what is ready. */
    private const val TEXTURE_TIMEOUT_MS = 12_000

    @Composable
    fun Ar(modelUrl: String, modifier: Modifier, onClose: () -> Unit) {
        val context = LocalContext.current
        var granted by remember {
            mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
        LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }
        val availability = remember { ArCoreApk.getInstance().checkAvailability(context) }

        Box(modifier) {
            when {
                !availability.isSupported -> ErrorText(stringResource(R.string.model_ar_unsupported), Modifier.align(Alignment.Center))
                !granted -> ErrorText(stringResource(R.string.model_ar_camera_permission), Modifier.align(Alignment.Center))
                else -> when (val fetch = rememberModelFile(modelUrl)) {
                    is Fetch.Loading -> LoadingModel(fetch.progress, Modifier.align(Alignment.Center))
                    is Fetch.Failed -> DownloadFailed(fetch, Modifier.align(Alignment.Center))
                    is Fetch.Idle -> IdleModel(fetch, Modifier.align(Alignment.Center))
                    is Fetch.Ready -> ArContent(fetch.file)
                }
            }
            IconButton(onClose, Modifier.align(Alignment.TopStart).padding(16.dp)) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.model_close_ar))
            }
        }
    }

    @Composable
    private fun ArContent(file: File) {
        val engine: Engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val childNodes = rememberNodes()
        var frame by remember { mutableStateOf<Frame?>(null) }
        val cache = ModelCache.get(LocalContext.current)
        val loaded = rememberModelInstance(modelLoader, file)
        val instance = (loaded as? Loaded.Ready)?.instance
        LaunchedEffect(loaded) { if ((loaded as? Loaded.Failed)?.badFile == true) cache.invalidate(file) } // do not keep a bad download
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = childNodes,
            engine = engine,
            modelLoader = modelLoader,
            planeRenderer = true,
            sessionConfiguration = { _, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            onSessionUpdated = { _, updated -> frame = updated },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { event, node ->
                    if (node == null && instance != null) {
                        val anchor = frame?.hitTest(event.x, event.y)?.firstOrNull { it.isValid(depthPoint = false, point = false) }?.createAnchorOrNull()
                        if (anchor != null) {
                            childNodes.clear() // one aircraft at a time, like iOS
                            childNodes += AnchorNode(engine = engine, anchor = anchor).apply {
                                addChildNode(ModelNode(modelInstance = instance, scaleToUnits = 0.6f, autoAnimate = true).apply { isEditable = true })
                            }
                        }
                    }
                },
            ),
        )
    }
}
