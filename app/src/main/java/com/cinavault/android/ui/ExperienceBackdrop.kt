package com.cinavault.android.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.cinavault.android.ui.theme.CinaVaultBlue
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultInk
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultOrchid
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ExperienceBackdrop(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cinavault-space")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18_000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbital-phase",
    )
    val pulse = transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(7_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "aurora-pulse",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        CinaVaultInk,
                        Color(0xFF08031A),
                        Color(0xFF031520),
                    ),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CinaVaultBlue.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(width * 0.12f, height * 0.08f),
                    radius = width * 0.6f * pulse.value,
                ),
                radius = width * 0.6f * pulse.value,
                center = Offset(width * 0.12f, height * 0.08f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CinaVaultMagenta.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(width * 0.92f, height * 0.18f),
                    radius = width * 0.5f,
                ),
                radius = width * 0.5f,
                center = Offset(width * 0.92f, height * 0.18f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CinaVaultCyan.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(width * 0.6f, height * 0.92f),
                    radius = width * 0.44f,
                ),
                radius = width * 0.44f,
                center = Offset(width * 0.6f, height * 0.92f),
            )

            val spacing = 46f
            var x = -spacing
            while (x < width + spacing) {
                drawLine(
                    color = CinaVaultCyan.copy(alpha = 0.055f),
                    start = Offset(x + phase.value * spacing, height * 0.38f),
                    end = Offset(x + phase.value * spacing, height),
                    strokeWidth = 1f,
                )
                x += spacing
            }
            var y = height * 0.38f
            while (y < height) {
                drawLine(
                    color = CinaVaultOrchid.copy(alpha = 0.05f),
                    start = Offset(0f, y + phase.value * spacing),
                    end = Offset(width, y + phase.value * spacing),
                    strokeWidth = 1f,
                )
                y += spacing
            }

            val center = Offset(width * 0.82f, height * 0.78f)
            repeat(3) { index ->
                val radius = 54f + index * 30f
                drawCircle(
                    color = if (index % 2 == 0) {
                        CinaVaultCyan.copy(alpha = 0.14f)
                    } else {
                        CinaVaultMagenta.copy(alpha = 0.12f)
                    },
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.4f),
                )
            }
            repeat(5) { index ->
                val angle = (phase.value * 360f + index * 72f) * (Math.PI / 180f)
                val radius = 54f + (index % 3) * 30f
                drawCircle(
                    color = if (index % 2 == 0) CinaVaultCyan else CinaVaultMagenta,
                    radius = 3.5f,
                    center = Offset(
                        center.x + cos(angle).toFloat() * radius,
                        center.y + sin(angle).toFloat() * radius,
                    ),
                )
            }
        }
    }
}
