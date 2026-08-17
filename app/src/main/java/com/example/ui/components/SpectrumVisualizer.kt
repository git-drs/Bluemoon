package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Song
import com.example.data.model.VisualizerMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpectrumVisualizer(
    mode: VisualizerMode,
    amplitudes: List<Float>,
    waveformEnergy: Float,
    song: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VisualizerTimeAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .testTag("spectrum_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            when (mode) {
                VisualizerMode.SPECTRUM -> {
                    // 16 Dual-Sided Neon Spectrum Bars
                    val numBars = amplitudes.size.coerceAtLeast(16)
                    val barWidth = (w * 0.8f) / (numBars * 1.5f)
                    val startX = (w - (numBars * 1.5f * barWidth)) / 2f
                    val baseLineY = h * 0.55f

                    for (i in 0 until numBars) {
                        val amp = (amplitudes.getOrElse(i) { 0.1f } * (if (isPlaying) 1f else 0.15f)).coerceIn(0.05f, 1.0f)
                        val barHeight = (h * 0.38f) * amp
                        val x = startX + i * (barWidth * 1.5f)

                        // Main Upward Bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    song.primaryColor,
                                    song.secondaryColor,
                                    Color.Transparent
                                ),
                                startY = baseLineY - barHeight,
                                endY = baseLineY
                            ),
                            topLeft = Offset(x, baseLineY - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Peak Cap Indicator
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(x, (baseLineY - barHeight - 4f).coerceAtLeast(10f)),
                            size = Size(barWidth, 3f),
                            cornerRadius = CornerRadius(1.5f, 1.5f)
                        )

                        // Mirrored Bottom Reflection (Lower opacity)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    song.primaryColor.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startY = baseLineY,
                                endY = baseLineY + (barHeight * 0.45f)
                            ),
                            topLeft = Offset(x, baseLineY + 3f),
                            size = Size(barWidth, barHeight * 0.45f),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }

                    // Ambient floor line
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, song.primaryColor.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        start = Offset(w * 0.05f, baseLineY),
                        end = Offset(w * 0.95f, baseLineY),
                        strokeWidth = 2f
                    )
                }

                VisualizerMode.LIQUID_ORB -> {
                    // Morphing Liquid Orb with Concentric Waves
                    val baseRadius = (w * 0.28f) * (1.0f + waveformEnergy * 0.3f)

                    // Outer Ripple Rings
                    for (ring in 1..4) {
                        val ringRad = baseRadius + ring * (22f + waveformEnergy * 15f)
                        val ringAlpha = (0.5f - ring * 0.1f).coerceAtLeast(0.05f)
                        drawCircle(
                            color = if (ring % 2 == 0) song.primaryColor.copy(alpha = ringAlpha) else song.secondaryColor.copy(alpha = ringAlpha),
                            radius = ringRad,
                            center = center,
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // Fluid Deforming Center Orb
                    val path = Path()
                    val points = 36
                    for (i in 0 until points) {
                        val angle = (i.toDouble() / points) * 2.0 * PI
                        val bandVal = amplitudes.getOrElse(i % amplitudes.size) { 0.2f }
                        val noise = sin(angle * 3.0 + time * 3.0).toFloat() * 12f +
                                cos(angle * 5.0 - time * 2.0).toFloat() * 8f
                        val rad = baseRadius + noise + (bandVal * 24f * (if (isPlaying) 1f else 0.2f))
                        val px = center.x + rad * cos(angle).toFloat()
                        val py = center.y + rad * sin(angle).toFloat()

                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    path.close()

                    // Draw Glowing Liquid Core
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                song.primaryColor.copy(alpha = 0.85f),
                                song.secondaryColor.copy(alpha = 0.65f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = baseRadius * 1.25f
                        )
                    )

                    // Inner bright nucleus
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = baseRadius * 0.25f * (1f + waveformEnergy * 0.5f),
                        center = center
                    )
                }

                VisualizerMode.AURORA -> {
                    // Flowing Aurora Ribbon Soundwaves & Particles
                    val wavePath1 = Path()
                    val wavePath2 = Path()

                    for (x in 0..w.toInt() step 6) {
                        val xRatio = x / w
                        val freqIndex = ((xRatio * amplitudes.size).toInt()).coerceIn(0, amplitudes.size - 1)
                        val amp = amplitudes[freqIndex] * (if (isPlaying) 1f else 0.2f)

                        val y1 = (h * 0.5f) + sin(xRatio * 4.0 * PI + time * 2.0).toFloat() * (35f + amp * 55f) +
                                cos(xRatio * 2.0 * PI - time).toFloat() * 20f
                        val y2 = (h * 0.54f) + cos(xRatio * 3.0 * PI - time * 1.8).toFloat() * (40f + amp * 50f)

                        if (x == 0) {
                            wavePath1.moveTo(x.toFloat(), y1)
                            wavePath2.moveTo(x.toFloat(), y2)
                        } else {
                            wavePath1.lineTo(x.toFloat(), y1)
                            wavePath2.lineTo(x.toFloat(), y2)
                        }
                    }

                    // Ribbon 1 (Primary Neon)
                    drawPath(
                        path = wavePath1,
                        brush = Brush.horizontalGradient(
                            listOf(song.secondaryColor, song.primaryColor, Color.White, song.primaryColor)
                        ),
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                    )

                    // Ribbon 2 (Secondary Glow)
                    drawPath(
                        path = wavePath2,
                        brush = Brush.horizontalGradient(
                            listOf(song.primaryColor, song.secondaryColor, song.primaryColor)
                        ),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // Floating Cosmic Energy Sparks
                    val particleCount = 28
                    for (p in 0 until particleCount) {
                        val pTime = (time * 0.4f + p * 0.35f) % 1.0f
                        val px = (w * ((p * 0.13f + 0.1f) % 0.85f))
                        val py = (h * (1.0f - pTime))
                        val pSize = (3.5f + sin(p * 2.0f).toFloat() * 2f).coerceAtLeast(1.5f)
                        val pAlpha = (sin(pTime * PI).toFloat()).coerceIn(0f, 1f)

                        drawCircle(
                            color = if (p % 2 == 0) song.primaryColor.copy(alpha = pAlpha) else Color.White.copy(alpha = pAlpha),
                            radius = pSize,
                            center = Offset(px, py)
                        )
                    }
                }

                VisualizerMode.VINYL, VisualizerMode.WAVEFORM, VisualizerMode.LYRICS -> {
                    // Handled by dedicated Views
                }
            }
        }
    }
}
