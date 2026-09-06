package com.raaveinm.arcadia.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Preview
@Composable
fun AmbientRandom(
    modifier: Modifier = Modifier,
    circleCount: Int = 16
) {
    val circles = remember { List(circleCount) { AmbientCircle() } }
    var isInitialized by remember { mutableStateOf(false) }
    var deltaMillis by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameMillis { frameTime ->
                if (lastTime != 0L) {
                    deltaMillis = (frameTime - lastTime).toFloat().coerceIn(0f, 64f)
                }
                lastTime = frameTime
            }
        }
    }

    val blur = 128
    Canvas(
        modifier = modifier.fillMaxSize().blur(blur.dp)
    ) {
        val dt = deltaMillis

        if (!isInitialized && size.width > 0f && size.height > 0f) {
            circles.forEach { it.respawn(size, randomizeProgress = true) }
            isInitialized = true
        }

        if (isInitialized) {
            circles.forEach { circle ->
                circle.update(dt, size)

                drawCircle(
                    brush = circle.brush,
                    radius = circle.radius,
                    center = circle.currentCenter,
                    alpha = circle.alpha,
                    blendMode = BlendMode.Color
                )
            }
        }
    }
}


private class AmbientCircle {
    private var baseCenter = Offset.Zero
    private var initialDrift = Offset.Zero
    private var targetDrift = Offset.Zero
    private var durationMillis = 1f
    private var elapsedMillis = 0f

    var radius = 0f
        private set
    var brush: Brush = Brush.sweepGradient(emptyList())
        private set

    fun respawn(size: Size, randomizeProgress: Boolean = false) {
        durationMillis = (9000L..27000L).random().toFloat()
        elapsedMillis = if (randomizeProgress) Random.nextFloat() * durationMillis else 0f
        radius = (142..384).random().toFloat()

        baseCenter = Offset(
            x = Random.nextDouble(0.0, size.width.toDouble().coerceAtLeast(1.0)).toFloat(),
            y = Random.nextDouble(0.0, size.height.toDouble().coerceAtLeast(1.0)).toFloat()
        )

        initialDrift = Offset(
            x = (0..64).random().toFloat(),
            y = (0..64).random().toFloat()
        )
        targetDrift = Offset(
            x = (0..64).random().toFloat(),
            y = (0..64).random().toFloat()
        )

        val listOfBrushes = listOf(
            Brush.radialGradient(
                colors = listOf(
                    Color.Cyan, Color.Magenta, Color.Blue
                ),
                radius = Random.nextDouble(0.0, size.height.toDouble().coerceAtLeast(1.0)).toFloat(),
                center = Offset(
                    Random.nextDouble(0.0, size.width.toDouble().coerceAtLeast(1.0)).toFloat(),
                    Random.nextDouble(0.0, size.height.toDouble().coerceAtLeast(1.0)).toFloat()
                )
            ),
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF683997), Color(0xFF2f8ed7), Color(0xFF2d73ff), Color(0xFF3b1f64)
                ),
                start = Offset(
                    Random.nextDouble(0.0, (size.width / 4.0).coerceAtLeast(1.0)).toFloat(),
                    Random.nextDouble(0.0, (size.height / 4.0).coerceAtLeast(1.0)).toFloat()
                ),
                end = Offset(
                    Random.nextDouble(0.0, ((size.width * 3) / 4.0).coerceAtLeast(1.0)).toFloat(),
                    Random.nextDouble(0.0, ((size.height * 3) / 4.0).coerceAtLeast(1.0)).toFloat()
                )
            ),
            Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF51103e), Color(0xFFbb244e), Color(0xFFf9bd2f), Color(0xFFdf5327)
                )
            )
        )
        brush = listOfBrushes.random()
    }

    fun update(deltaMillis: Float, size: Size) {
        elapsedMillis += deltaMillis
        if (elapsedMillis >= durationMillis) {
            respawn(size)
        }
    }

    private val progress: Float
        get() = (elapsedMillis / durationMillis).coerceIn(0f, 1f)

    val alpha: Float
        get() = sin(progress * PI).toFloat().coerceIn(0f, 1f)

    val currentCenter: Offset
        get() {
            val driftX = initialDrift.x + (targetDrift.x - initialDrift.x) * progress
            val driftY = initialDrift.y + (targetDrift.y - initialDrift.y) * progress
            return Offset(baseCenter.x + driftX, baseCenter.y + driftY)
        }
}