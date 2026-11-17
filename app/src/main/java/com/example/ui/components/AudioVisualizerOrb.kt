package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.MagentaPulse
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun AudioVisualizerOrb(
    orbState: OrbState,
    audioRmsLevel: Float,
    onTapOrb: () -> Unit,
    modifier: Modifier = Modifier,
    orbSize: Dp = 148.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_motion")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.THINKING -> 2200
                    OrbState.LISTENING -> 3500
                    OrbState.SPEAKING -> 3000
                    OrbState.IDLE -> 9000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_spin"
    )

    val breath by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_breath"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_wave"
    )

    val smoothEnergy by animateFloatAsState(
        targetValue = when (orbState) {
            OrbState.IDLE -> 0.12f
            OrbState.LISTENING -> (0.35f + audioRmsLevel.coerceIn(0f, 1f) * 0.65f)
            OrbState.THINKING -> 0.78f
            OrbState.SPEAKING -> 0.72f
        },
        animationSpec = tween(140),
        label = "orb_energy"
    )

    val statusLabel = when (orbState) {
        OrbState.IDLE -> "TAP ORB TO SPEAK"
        OrbState.LISTENING -> "LISTENING TO VOICE..."
        OrbState.THINKING -> "SYNTHESIZING RESPONSE..."
        OrbState.SPEAKING -> "VOICE OUTPUT ACTIVE"
    }

    val statusColor = when (orbState) {
        OrbState.IDLE -> TextSecondary
        OrbState.LISTENING -> EmeraldGlow
        OrbState.THINKING -> VividPurple
        OrbState.SPEAKING -> NeonCyan
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(orbSize)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, color = NeonCyan),
                    onClick = onTapOrb
                )
                .semantics { contentDescription = "Kainat AI voice orb: $statusLabel" }
                .testTag("voice_visualizer_orb"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(orbSize)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2f) * 0.56f * breath

                // Outer ambient energy glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.35f * smoothEnergy + 0.1f),
                            VividPurple.copy(alpha = 0.22f * smoothEnergy + 0.05f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension * 0.5f
                    ),
                    radius = size.minDimension * 0.5f,
                    center = center
                )

                // Reactive harmonic outer waveform ring
                val wavePath = Path()
                val ringRadius = baseRadius * (1.16f + 0.14f * smoothEnergy)
                val points = 72
                for (i in 0..points) {
                    val theta = (i.toFloat() / points) * (2f * PI.toFloat())
                    val harmonic = sin(theta * 5f + wavePhase) * cos(theta * 3f - wavePhase)
                    val r = ringRadius + harmonic * (baseRadius * 0.22f * smoothEnergy)
                    val x = center.x + r * cos(theta)
                    val y = center.y + r * sin(theta)
                    if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                }
                wavePath.close()

                drawPath(
                    path = wavePath,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            NeonCyan,
                            ElectricBlue,
                            VividPurple,
                            MagentaPulse,
                            NeonCyan
                        ),
                        center = center
                    ),
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Rotating orbital plasma rings
                rotate(degrees = spinAngle, pivot = center) {
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.85f),
                                Color.Transparent,
                                VividPurple.copy(alpha = 0.85f)
                            )
                        ),
                        topLeft = Offset(center.x - baseRadius * 1.28f, center.y - baseRadius * 0.62f),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 2.56f, baseRadius * 1.24f),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                }

                rotate(degrees = -spinAngle * 0.7f + 60f, pivot = center) {
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.75f),
                                Color.Transparent,
                                MagentaPulse.copy(alpha = 0.75f)
                            )
                        ),
                        topLeft = Offset(center.x - baseRadius * 1.22f, center.y - baseRadius * 0.54f),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 2.44f, baseRadius * 1.08f),
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                }

                // 3D liquid glass core sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            NeonCyan.copy(alpha = 0.85f),
                            ElectricBlue.copy(alpha = 0.78f),
                            VividPurple.copy(alpha = 0.75f),
                            Color(0xFF0A0618)
                        ),
                        center = Offset(
                            center.x - baseRadius * 0.25f,
                            center.y - baseRadius * 0.28f
                        ),
                        radius = baseRadius * 1.25f
                    ),
                    radius = baseRadius,
                    center = center
                )

                // Specular glass crescent highlight
                drawArc(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.75f),
                            Color.Transparent
                        )
                    ),
                    startAngle = 200f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.82f, center.y - baseRadius * 0.82f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 1.64f, baseRadius * 1.64f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor
        )
    }
}
