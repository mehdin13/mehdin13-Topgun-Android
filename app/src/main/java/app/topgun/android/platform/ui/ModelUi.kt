// ANDROID-ONLY
package app.topgun.android.platform.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
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
        data object Loading : Fetch
        data object Failed : Fetch
        data class Ready(val file: File) : Fetch
    }

    private sealed interface Loaded {
        data object Loading : Loaded
        data class Failed(val reason: String) : Loaded
        data class Ready(val instance: ModelInstance) : Loaded
    }

    @Composable
    private fun rememberModelFile(url: String): Fetch {
        val context = LocalContext.current
        var state by remember(url) { mutableStateOf<Fetch>(Fetch.Loading) }
        LaunchedEffect(url) {
            val file = ModelCache(context.applicationContext).fetch(url)
            state = if (file != null) Fetch.Ready(file) else Fetch.Failed
        }
        return state
    }

    /** Reads the file off the main thread into the direct buffer Filament needs. Throws when the file is not a glb/gltf (e.g. an HTML error page). */
    private suspend fun readModelBuffer(file: File): ByteBuffer = withContext(Dispatchers.IO) {
        val bytes = file.readBytes()
        val isGlb = bytes.size > 12 && bytes[0] == 'g'.code.toByte() && bytes[1] == 'l'.code.toByte() && bytes[2] == 'T'.code.toByte() && bytes[3] == 'F'.code.toByte()
        val isGltfJson = file.name.endsWith(".gltf", ignoreCase = true) && bytes.isNotEmpty() && bytes[0] == '{'.code.toByte()
        if (!isGlb && !isGltfJson) {
            file.delete() // do not keep a bad download in the cache
            throw IOException("The downloaded file is not a glb model")
        }
        ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); rewind() }
    }

    /**
     * Filament objects must be created on the main thread; only the file read happens on IO. A model that fails to
     * parse is reported as [Loaded.Failed] instead of crashing.
     */
    @Composable
    private fun rememberModelInstance(loader: ModelLoader, file: File): Loaded =
        produceState<Loaded>(Loaded.Loading, loader, file) {
            value = try {
                val buffer = readModelBuffer(file)
                Loaded.Ready(loader.createModelInstance(buffer))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) { // Filament throws NPE / IllegalStateException for broken assets
                Loaded.Failed(e.message ?: "unknown error")
            }
        }.value

    @Composable
    private fun ErrorText(text: String, modifier: Modifier = Modifier) =
        Text(text, style = TGText.sailecMedium(14), color = TG.colors.textSecondary, textAlign = TextAlign.Center, modifier = modifier.padding(TGSpace.l))

    @Composable
    fun Viewer(modelUrl: String, modifier: Modifier) {
        Box(modifier.background(TG.colors.card), contentAlignment = Alignment.Center) {
            when (val fetch = rememberModelFile(modelUrl)) {
                Fetch.Loading -> CircularProgressIndicator(color = TG.colors.accent)
                Fetch.Failed -> ErrorText("Could not download the 3D model. Check your connection, or use Open in 3D viewer.")
                is Fetch.Ready -> {
                    val engine = rememberEngine()
                    val modelLoader = rememberModelLoader(engine)
                    // The manipulator rebuilds the camera every frame from its own home position, so the home position
                    // (not only cameraNode.position) must be set or the camera starts inside the model.
                    val home = Float3(0f, 0f, CAMERA_DISTANCE)
                    val cameraNode = rememberCameraNode(engine) { position = home }
                    val manipulator = rememberCameraManipulator(orbitHomePosition = home, targetPosition = Float3(0f, 0f, 0f))
                    when (val loaded = rememberModelInstance(modelLoader, fetch.file)) {
                        Loaded.Loading -> CircularProgressIndicator(color = TG.colors.accent)
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
                    Fetch.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = TG.colors.accent)
                    Fetch.Failed -> ErrorText("Could not load the 3D model.", Modifier.align(Alignment.Center))
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
