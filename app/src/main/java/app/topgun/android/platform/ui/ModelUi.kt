// ANDROID-ONLY
package app.topgun.android.platform.ui

import android.Manifest
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
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
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
        data class Ready(val file: File) : Fetch
    }

    /** The downloaded file is not a glb/gltf (for example an HTML error page). */
    private class BadModelFile : IOException("The downloaded file is not a glb model")

    private sealed interface Loaded {
        data object Loading : Loaded
        data class Failed(val reason: String) : Loaded
        data class Ready(val instance: ModelInstance) : Loaded
    }

    /**
     * Gets the model through the shared [ModelCache]: a downloaded file is used at once, otherwise it is downloaded and
     * the live progress is reported as [Fetch.Loading]. Leaving the screen stops a download that only the viewer started.
     */
    @Composable
    private fun rememberModelFile(url: String): Fetch {
        val cache = ModelCache.get(LocalContext.current)
        var attempt by remember(url) { mutableIntStateOf(0) }
        var file by remember(url, attempt) { mutableStateOf<File?>(null) }
        var failed by remember(url, attempt) { mutableStateOf(false) }
        val state by remember(url) { cache.state(url) }.collectAsState()
        LaunchedEffect(url, attempt) {
            val result = cache.fetch(url)
            file = result
            failed = result == null
        }
        DisposableEffect(url) { onDispose { cache.release(url) } }
        val ready = file
        return when {
            ready != null -> Fetch.Ready(ready)
            failed -> Fetch.Failed((state as? ModelDownloadState.Failed)?.message) { attempt++ }
            else -> Fetch.Loading(state as? ModelDownloadState.Downloading)
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
                Text("Preparing 3D model…", style = TGText.sailecMedium(14), color = TG.colors.textSecondary)
            }
        }
    }

    @Composable
    private fun DownloadFailed(fetch: Fetch.Failed, modifier: Modifier = Modifier) {
        Column(modifier.padding(TGSpace.l), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TGSpace.m)) {
            ErrorText("Could not download the 3D model. Check your connection and try again, or use Open in 3D viewer." + (fetch.message?.let { " ($it)" } ?: ""))
            Button(onClick = fetch.retry, modifier = Modifier.defaultMinSize(minHeight = TGSize.tapMin)) { Text("Try again", style = TGText.sailecMedium(16)) }
        }
    }

    /** Reads the file off the main thread into the direct buffer Filament needs. Throws when the file is not a glb/gltf (e.g. an HTML error page). */
    private suspend fun readModelBuffer(file: File): ByteBuffer = withContext(Dispatchers.IO) {
        val bytes = file.readBytes()
        val isGlb = bytes.size > 12 && bytes[0] == 'g'.code.toByte() && bytes[1] == 'l'.code.toByte() && bytes[2] == 'T'.code.toByte() && bytes[3] == 'F'.code.toByte()
        val isGltfJson = file.name.endsWith(".gltf", ignoreCase = true) && bytes.isNotEmpty() && bytes[0] == '{'.code.toByte()
        if (!isGlb && !isGltfJson) {
            throw BadModelFile()
        }
        ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); rewind() }
    }

    /**
     * Filament objects must be created on the main thread; only the file read happens on IO. A model that fails to
     * parse is reported as [Loaded.Failed] instead of crashing.
     */
    @Composable
    private fun rememberModelInstance(loader: ModelLoader, file: File): Loaded {
        val cache = ModelCache.get(LocalContext.current)
        return produceState<Loaded>(Loaded.Loading, loader, file) {
            value = try {
                val buffer = readModelBuffer(file)
                Loaded.Ready(loader.createModelInstance(buffer))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) { // Filament throws NPE / IllegalStateException for broken assets
                if (e is BadModelFile) cache.invalidate(file) // do not keep a bad download
                Loaded.Failed(e.message ?: "unknown error")
            }
        }.value
    }

    @Composable
    private fun ErrorText(text: String, modifier: Modifier = Modifier) =
        Text(text, style = TGText.sailecMedium(14), color = TG.colors.textSecondary, textAlign = TextAlign.Center, modifier = modifier.padding(TGSpace.l))

    @Composable
    fun Viewer(modelUrl: String, modifier: Modifier) {
        Box(modifier.background(TG.colors.card), contentAlignment = Alignment.Center) {
            when (val fetch = rememberModelFile(modelUrl)) {
                is Fetch.Loading -> LoadingModel(fetch.progress)
                is Fetch.Failed -> DownloadFailed(fetch)
                is Fetch.Ready -> {
                    val engine = rememberEngine()
                    val modelLoader = rememberModelLoader(engine)
                    // The manipulator rebuilds the camera every frame from its own home position, so the home position
                    // (not only cameraNode.position) must be set or the camera starts inside the model.
                    val home = Float3(0f, 0f, CAMERA_DISTANCE)
                    val cameraNode = rememberCameraNode(engine) { position = home }
                    val manipulator = rememberCameraManipulator(orbitHomePosition = home, targetPosition = Float3(0f, 0f, 0f))
                    when (val loaded = rememberModelInstance(modelLoader, fetch.file)) {
                        Loaded.Loading -> LoadingModel(null)
                        is Loaded.Failed -> ErrorText("This 3D model could not be displayed (${loaded.reason}). Try Open in 3D viewer.")
                        is Loaded.Ready -> {
                            val nodes = rememberNodes {
                                add(
                                    ModelNode(modelInstance = loaded.instance, scaleToUnits = MODEL_UNITS, autoAnimate = true).apply {
                                        // Centre the model on the orbit target: scale about the node origin, then shift by the scaled bbox centre.
                                        val k = scale.x
                                        position = Float3(-center.x * k, -center.y * k, -center.z * k)
                                    },
                                )
                            }
                            Scene(
                                modifier = Modifier.fillMaxSize(),
                                engine = engine,
                                modelLoader = modelLoader,
                                cameraNode = cameraNode,
                                cameraManipulator = manipulator,
                                childNodes = nodes,
                            )
                        }
                    }
                }
            }
        }
    }

    /** Fits the longest side of the aircraft into this many Filament units (metres). */
    private const val MODEL_UNITS = 1f
    private const val CAMERA_DISTANCE = 2.2f

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
                !availability.isSupported -> ErrorText("AR is not supported on this device.", Modifier.align(Alignment.Center))
                !granted -> ErrorText("Camera permission is needed to place the aircraft.", Modifier.align(Alignment.Center))
                else -> when (val fetch = rememberModelFile(modelUrl)) {
                    is Fetch.Loading -> LoadingModel(fetch.progress, Modifier.align(Alignment.Center))
                    is Fetch.Failed -> DownloadFailed(fetch, Modifier.align(Alignment.Center))
                    is Fetch.Ready -> ArContent(fetch.file)
                }
            }
            IconButton(onClose, Modifier.align(Alignment.TopStart).padding(16.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Close AR")
            }
        }
    }

    @Composable
    private fun ArContent(file: File) {
        val engine: Engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val childNodes = rememberNodes()
        var frame by remember { mutableStateOf<Frame?>(null) }
        val instance = (rememberModelInstance(modelLoader, file) as? Loaded.Ready)?.instance
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
