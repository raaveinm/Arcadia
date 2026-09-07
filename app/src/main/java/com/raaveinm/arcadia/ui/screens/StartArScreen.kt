package com.raaveinm.arcadia.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.raaveinm.arcadia.R
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import java.util.concurrent.atomic.AtomicReference

@SuppressLint("DefaultLocale")
@Composable
fun StartArScreen() {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    var rotationY by remember { mutableFloatStateOf(.0f) }
    var modelScale by remember { mutableFloatStateOf(.4f) }
    val currentFrame = remember { AtomicReference<Frame?>() }
    val anchorMissedStatus = stringResource(R.string.find_anchor)
    var status by remember { mutableStateOf("") }
    val modelInstance = remember { modelLoader.createModelInstance("models/flower.glb") }
    val unitScale = remember(modelInstance) {
        val halfExtent = modelInstance.asset.boundingBox.halfExtent
        1f / (2f * maxOf(halfExtent[0], halfExtent[1], halfExtent[2]))
    }
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            sessionConfiguration = { _, config ->
                config.depthMode = Config.DepthMode.AUTOMATIC
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            },
            planeRenderer = false,
            onSessionUpdated = { session, frame ->
                currentFrame.set(frame)
                val hasTrackingPlanes = session.getAllTrackables(Plane::class.java).any {
                    it.trackingState == TrackingState.TRACKING
                }
                status = if (hasTrackingPlanes) "" else anchorMissedStatus
            },
        ) {
            ModelNode(
                modelInstance = modelInstance,
                centerOrigin = Position(.0f, -1f, .0f),
                position = Position(.0f, -.2f, -1.2f),
                rotation = Rotation(0f, rotationY, 0f),
                scale = Scale(modelScale * unitScale)
            )
        }

        if (status.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            Card(
                modifier = Modifier

                    .padding(16.dp, 16.dp, 16.dp, 64.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Params",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(text = "Rotation: ${rotationY.toInt()}°")
                    Slider(
                        value = rotationY,
                        onValueChange = { rotationY = it },
                        valueRange = 0f..360f
                    )

                    Text(text = "Scale: ${String.format("%.2f", modelScale)}")
                    Slider(
                        value = modelScale,
                        onValueChange = { modelScale = it },
                        valueRange = 0.1f..1.0f
                    )

                    Button(
                        onClick = {
                            rotationY = 0f
                            modelScale = 0.4f
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset")
                    }
                }
            }
        }
        IconButton(
            modifier = Modifier.align(Alignment.BottomEnd).zIndex(1f).padding(16.dp),
            onClick = {isExpanded = !isExpanded}
        ) {
            Icon(imageVector = if (!isExpanded) Icons.Default.KeyboardDoubleArrowUp else Icons.Default.KeyboardDoubleArrowDown
            ,null)
        }
    }
}