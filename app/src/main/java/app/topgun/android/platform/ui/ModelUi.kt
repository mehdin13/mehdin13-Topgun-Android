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

    private fun load(loader: ModelLoader, file: File): ModelInstance? = runCatching {
        val bytes = file.readBytes()
        val buffer = ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); rewind() }
        loader.createModelInstance(buffer)
    }.getOrNull()

    @Composable
    fun Viewer(modelUrl: String, modifier: Modifier) {
        Box(modifier, contentAlignment = Alignment.Center) {
            when (val fetch = rememberModelFile(modelUrl)) {
                Fetch.Loading -> CircularProgressIndicator()
                Fetch.Failed -> Text("Could not load the 3D model.")
                is Fetch.Ready -> {
                    val engine = rememberEngine()
                    val modelLoader = rememberModelLoader(engine)
                    val cameraNode = rememberCameraNode(engine) { position = Float3(0f, 0f, 4f) }
                    val instance = remember(fetch.file) { load(modelLoader, fetch.file) }
                    if (instance == null) {
                        Text("Could not load the 3D model.")
                    } else {
                        val nodes = rememberNodes {
                            add(ModelNode(modelInstance = instance, scaleToUnits = 1.5f, autoAnimate = true))
                        }
                        Scene(
                            modifier = Modifier.fillMaxSize(),
                            engine = engine,
                            modelLoader = modelLoader,
                            cameraNode = cameraNode,
                            cameraManipulator = rememberCameraManipulator(),
                            childNodes = nodes,
                        )
                    }
                }
            }
        }
    }

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
                !availability.isSupported -> Text("AR is not supported on this device.", Modifier.align(Alignment.Center))
                !granted -> Text("Camera permission is needed to place the aircraft.", Modifier.align(Alignment.Center))
                else -> when (val fetch = rememberModelFile(modelUrl)) {
                    Fetch.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    Fetch.Failed -> Text("Could not load the 3D model.", Modifier.align(Alignment.Center))
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
        val instance = remember(file) { load(modelLoader, file) }
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
