package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Selah Orb: A glossy 3D-looking sphere made of swirling gradients
 * (coral → peach → lavender → sky blue) with layered radial/conic gradients,
 * slow rotation, breathing pulse, and a thin wavy halo ring (scalloped circle)
 * that ripples when active.
 */
@Composable
fun SelahOrb(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isPulsing: Boolean = true,
    isListening: Boolean = false,
    tintColor: Color? = null,
    haloScaleExtra: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "selah_orb")

    // Slow rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 4000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    // Gentle breathing pulse
    val breathing by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.08f else if (isPulsing) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 1200 else 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_breathing"
    )

    // Halo ripple
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 1000 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_ripple"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val orbRadius = (this.size.minDimension / 2f) * 0.72f * breathing

            // 1. Wavy Scalloped Halo Ring
            val haloRadius = (this.size.minDimension / 2f) * 0.88f * (haloPulse + haloScaleExtra)
            val wavePath = Path()
            val scallops = 14
            val waveAmplitude = haloRadius * 0.05f

            for (i in 0..scallops * 10) {
                val angle = (i.toFloat() / (scallops * 10f)) * 2f * PI.toFloat()
                val r = haloRadius + sin(angle * scallops) * waveAmplitude
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
            }
            wavePath.close()

            rotate(rotation * 0.3f, pivot = center) {
                drawPath(
                    path = wavePath,
                    color = (tintColor ?: Color(0xFFFFB59A)).copy(alpha = if (isListening) 0.6f else 0.35f),
                    style = Stroke(width = if (size > 100.dp) 3.dp.toPx() else 1.5.dp.toPx())
                )
            }

            // Outer soft ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (tintColor ?: Color(0xFFFF7A4D)).copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius * 1.5f
                ),
                radius = orbRadius * 1.5f,
                center = center
            )

            // 2. Base Sphere with Swirling Gradients
            rotate(rotation, pivot = center) {
                val primaryColor = tintColor ?: Color(0xFFFF5A2C)
                val peachColor = Color(0xFFFFB59A)
                val lavColor = Color(0xFFD3BFFF)
                val skyColor = Color(0xFF7DB5F5)

                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        peachColor,
                        lavColor,
                        skyColor,
                        primaryColor
                    ),
                    center = center
                )

                drawCircle(
                    brush = gradientBrush,
                    radius = orbRadius,
                    center = center
                )
            }

            // 3. Inner 3D spherical depth shading (radial shading offset toward top-left)
            val highlightOffset = Offset(center.x - orbRadius * 0.28f, center.y - orbRadius * 0.28f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Black.copy(alpha = 0.35f)
                    ),
                    center = highlightOffset,
                    radius = orbRadius * 1.1f
                ),
                radius = orbRadius,
                center = center
            )

            // 4. Glossy Specular Highlight (Reflection ellipse near top edge)
            val specWidth = orbRadius * 0.55f
            val specHeight = orbRadius * 0.25f
            val specCenter = Offset(center.x - orbRadius * 0.18f, center.y - orbRadius * 0.42f)

            rotate(-25f, pivot = specCenter) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        center = specCenter,
                        radius = specWidth
                    ),
                    topLeft = Offset(specCenter.x - specWidth / 2, specCenter.y - specHeight / 2),
                    size = androidx.compose.ui.geometry.Size(specWidth, specHeight)
                )
            }
        }
    }
}
