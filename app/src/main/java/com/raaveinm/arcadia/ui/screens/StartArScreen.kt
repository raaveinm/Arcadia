package com.raaveinm.arcadia.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deblur
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.raaveinm.arcadia.R
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.firstByTypeOrNull
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.java

@Composable
fun StartArScreen() {
    val engine = rememberEngine()
    val modeLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val redDotMaterial = remember(materialLoader) {
        materialLoader.createUnlitColorInstance(Color.Red)
    }
    val placedAnchors = remember { mutableStateListOf<Anchor>() }
    var status by remember { mutableStateOf("") }
    val anchorMissedStatus = stringResource(R.string.find_anchor)
    val currentFrame = remember { AtomicReference<Frame?>() }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modeLoader,
            sessionConfiguration = { _, config ->
                config.depthMode = Config.DepthMode.AUTOMATIC
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            },
            planeRenderer = true,
            onSessionUpdated = { session, frame ->
                currentFrame.set(frame)
                val hasTrackingPlanes = session.getAllTrackables(Plane::class.java).any {
                    it.trackingState == TrackingState.TRACKING
                }
                status = if (hasTrackingPlanes) "" else anchorMissedStatus
            },
            onTouchEvent = { motionEvent, hitResult ->
                if (hitResult == null && motionEvent.action == MotionEvent.ACTION_UP) {
                    val anchor = currentFrame.get()
                        ?.hitTest(motionEvent)
                        ?.firstByTypeOrNull(
                            planeTypes = Plane.Type.entries.toSet(),
                            point = true,
                            depthPoint = true,
                            instantPlacementPoint = true
                        )
                        ?.createAnchorOrNull()
                    anchor?.let { placedAnchors.add(it) }
                    anchor != null
                } else {
                    false
                }
            }
        ) {
            placedAnchors.forEach { anchor ->
                AnchorNode(anchor = anchor) {
                    SphereNode(
                        radius = 0.015f,
                        materialInstance = redDotMaterial
                    )
                }
            }
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

        if (placedAnchors.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    placedAnchors.forEach { it.detach() }
                    placedAnchors.clear()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Default.Deblur , contentDescription = "clear_scene")
            }
        }
    }
}