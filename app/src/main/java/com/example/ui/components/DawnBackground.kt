package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Dawn50
import com.example.ui.theme.LavenderWash
import com.example.ui.theme.NightBase
import com.example.ui.theme.NightIndigo
import com.example.ui.theme.NightSurface
import com.example.ui.theme.RoseWash
import com.example.ui.theme.Sky100
import kotlin.math.cos
import kotlin.math.sin

/**
 * DawnBackground: Ambient sunrise mesh gradient with subtle shifting light streaks.
 */
@Composable
fun DawnBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "ambient_dawn")

    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) NightBase else Dawn50)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (isDark) {
                // Night Vigil deep indigo & obsidian glow
                val center1 = Offset(width * 0.2f + sin(drift) * 60f, height * 0.3f + cos(drift) * 80f)
                val center2 = Offset(width * 0.8f + cos(drift) * 60f, height * 0.7f + sin(drift) * 80f)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NightIndigo.copy(alpha = 0.5f), Color.Transparent),
                        center = center1,
                        radius = width * 0.85f
                    ),
                    center = center1,
                    radius = width * 0.85f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF352B4D).copy(alpha = 0.45f), Color.Transparent),
                        center = center2,
                        radius = width * 0.75f
                    ),
                    center = center2,
                    radius = width * 0.75f
                )
            } else {
                // Dawn Glass mesh: Dawn-100 top-left -> Rose center -> Lavender -> Sky-100
                val c1 = Offset(width * 0.15f + sin(drift) * 50f, height * 0.15f + cos(drift) * 60f)
                val c2 = Offset(width * 0.85f + cos(drift) * 50f, height * 0.4f + sin(drift) * 50f)
                val c3 = Offset(width * 0.25f + sin(drift * 0.7f) * 60f, height * 0.85f + cos(drift * 0.7f) * 60f)

                // Peach / coral wash top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Dawn100.copy(alpha = 0.85f), Color.Transparent),
                        center = c1,
                        radius = width * 0.95f
                    ),
                    center = c1,
                    radius = width * 0.95f
                )

                // Soft Rose center-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RoseWash.copy(alpha = 0.65f), Color.Transparent),
                        center = c2,
                        radius = width * 0.9f
                    ),
                    center = c2,
                    radius = width * 0.9f
                )

                // Lavender & Sky bottom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LavenderWash.copy(alpha = 0.75f), Sky100.copy(alpha = 0.45f), Color.Transparent),
                        center = c3,
                        radius = width * 1.1f
                    ),
                    center = c3,
                    radius = width * 1.1f
                )

                // Faint vertical light streaks (morning window light)
                val streakAlpha = 0.08f + sin(drift * 2f) * 0.02f
                drawLine(
                    color = Color.White.copy(alpha = streakAlpha),
                    start = Offset(width * 0.35f, 0f),
                    end = Offset(width * 0.25f, height),
                    strokeWidth = width * 0.2f
                )
                drawLine(
                    color = Color.White.copy(alpha = streakAlpha * 0.7f),
                    start = Offset(width * 0.75f, 0f),
                    end = Offset(width * 0.65f, height),
                    strokeWidth = width * 0.16f
                )
            }
        }

        content()
    }
}
