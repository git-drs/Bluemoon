package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DeepVoid
import com.example.ui.theme.RichCharcoal
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmbientGlowBackground(
    primaryColor: Color,
    secondaryColor: Color,
    waveformEnergy: Float = 0.2f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedPrimary by animateColorAsState(
        targetValue = primaryColor,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "PrimaryColorAnim"
    )

    val animatedSecondary by animateColorAsState(
        targetValue = secondaryColor,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "SecondaryColorAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "AmbientFlow")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AmbientPhase"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base deep void fill
            drawRect(color = DeepVoid)

            // Dynamic moving radial glow 1 (Top Left / Center)
            val glowRadius1 = (width * 0.75f) * (1.0f + waveformEnergy * 0.25f)
            val glowCenter1 = Offset(
                x = width * 0.3f + sin(phase) * (width * 0.15f),
                y = height * 0.25f + cos(phase) * (height * 0.1f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedPrimary.copy(alpha = 0.42f + waveformEnergy * 0.18f),
                        animatedPrimary.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = glowCenter1,
                    radius = glowRadius1
                ),
                radius = glowRadius1,
                center = glowCenter1
            )

            // Dynamic moving radial glow 2 (Bottom Right / Middle)
            val glowRadius2 = (width * 0.85f) * (1.0f + waveformEnergy * 0.2f)
            val glowCenter2 = Offset(
                x = width * 0.7f - cos(phase * 0.8f) * (width * 0.2f),
                y = height * 0.65f - sin(phase * 0.8f) * (height * 0.15f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedSecondary.copy(alpha = 0.38f + waveformEnergy * 0.15f),
                        animatedSecondary.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = glowCenter2,
                    radius = glowRadius2
                ),
                radius = glowRadius2,
                center = glowCenter2
            )

            // Dark overlay vignette gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepVoid.copy(alpha = 0.35f),
                        RichCharcoal.copy(alpha = 0.2f),
                        DeepVoid.copy(alpha = 0.75f),
                        DeepVoid.copy(alpha = 0.95f)
                    )
                )
            )
        }

        content()
    }
}
