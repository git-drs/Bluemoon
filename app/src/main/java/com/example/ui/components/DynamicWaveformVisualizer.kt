package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.PastelLavender
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance Dynamic Waveform Audio Visualizer using Jetpack Compose Canvas.
 *
 * Renders in real-time:
 * 1. Continuous harmonic multi-frequency waveform curves reactive to live amplitude & FFT bands.
 * 2. Translucent gradient filled aura beneath the wave curves.
 * 3. Mirrored reflection wave below baseline.
 * 4. Dual-sided floating discrete amplitude frequency stems with peak caps.
 * 5. Dynamic spark particles traversing wave peaks based on transient audio energy.
 * 6. Center baseline with neon glow and dB/RMS energy telemetry badge.
 */
@Composable
fun DynamicWaveformVisualizer(
    amplitudes: List<Float>,
    waveformEnergy: Float,
    song: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformPhase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveformPhaseAnimation"
    )

    val fastPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (4f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveformFastPhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .testTag("dynamic_waveform_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerY = h * 0.52f
            val playFactor = if (isPlaying) 1.0f else 0.18f
            val energy = (waveformEnergy * playFactor).coerceIn(0.02f, 1.0f)

            // 1. Draw Discrete Multi-Band Amplitude Frequency Stems (Background Stems)
            drawAmplitudeStems(
                w = w,
                h = h,
                centerY = centerY,
                amplitudes = amplitudes,
                playFactor = playFactor,
                primaryColor = song.primaryColor,
                secondaryColor = song.secondaryColor
            )

            // 2. Draw Translucent Gradient Aura Under the Waveform
            drawWaveformFilledAura(
                w = w,
                h = h,
                centerY = centerY,
                amplitudes = amplitudes,
                phase = phase,
                energy = energy,
                primaryColor = song.primaryColor,
                secondaryColor = song.secondaryColor
            )

            // 3. Draw Mirrored Reflection Wave (Subtle lower glow)
            drawMirroredWaveform(
                w = w,
                h = h,
                centerY = centerY,
                amplitudes = amplitudes,
                phase = phase,
                energy = energy,
                primaryColor = song.primaryColor
            )

            // 4. Draw Main Primary High-Energy Sine/Harmonic Spline Waveform
            drawPrimaryWaveCurve(
                w = w,
                h = h,
                centerY = centerY,
                amplitudes = amplitudes,
                phase = phase,
                energy = energy,
                primaryColor = song.primaryColor,
                secondaryColor = song.secondaryColor
            )

            // 5. Draw Secondary Harmonic Cross-Wave
            drawSecondaryWaveCurve(
                w = w,
                h = h,
                centerY = centerY,
                amplitudes = amplitudes,
                fastPhase = fastPhase,
                energy = energy,
                secondaryColor = song.secondaryColor
            )

            // 6. Draw High-Energy Audio Peak Spark Particles
            drawEnergyParticles(
                w = w,
                h = h,
                centerY = centerY,
                energy = energy,
                phase = phase,
                primaryColor = song.primaryColor
            )

            // 7. Center Neon Baseline Glow
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        song.primaryColor.copy(alpha = 0.6f * playFactor + 0.1f),
                        EditorialIceBlue.copy(alpha = 0.8f * playFactor + 0.1f),
                        song.secondaryColor.copy(alpha = 0.6f * playFactor + 0.1f),
                        Color.Transparent
                    )
                ),
                start = Offset(w * 0.04f, centerY),
                end = Offset(w * 0.96f, centerY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Real-Time Audio Energy & Telemetry Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-8).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xDD121022))
                .border(1.dp, song.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = "Real-time Waveform",
                    tint = if (isPlaying) EditorialIceBlue else PastelLavender,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val energyPct = ((waveformEnergy * (if (isPlaying) 1f else 0.1f)).coerceIn(0f, 1f) * 100).toInt()
                Text(
                    text = if (isPlaying) "PEAK $energyPct%" else "PAUSED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPlaying) EditorialIceBlue else PastelLavender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
            }
        }
    }
}

/**
 * Draws discrete vertical frequency amplitude bars positioned symmetrically along baseline.
 */
private fun DrawScope.drawAmplitudeStems(
    w: Float,
    h: Float,
    centerY: Float,
    amplitudes: List<Float>,
    playFactor: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val barCount = 32
    val usableWidth = w * 0.90f
    val startX = w * 0.05f
    val slotWidth = usableWidth / barCount
    val barWidth = (slotWidth * 0.52f).coerceAtLeast(3f)

    for (i in 0 until barCount) {
        val ampIndex = (i * amplitudes.size / barCount).coerceIn(0, amplitudes.size - 1)
        val rawAmp = amplitudes.getOrElse(ampIndex) { 0.12f }
        val effectiveAmp = (rawAmp * playFactor).coerceIn(0.06f, 1.0f)

        val maxBarHeight = h * 0.36f
        val barHeight = maxBarHeight * effectiveAmp
        val x = startX + (i * slotWidth) + (slotWidth - barWidth) / 2f

        // Upward Frequency Stem
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.85f),
                    secondaryColor.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                startY = centerY - barHeight,
                endY = centerY
            ),
            topLeft = Offset(x, centerY - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        // Downward Mirror Stem (Dimmer)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                startY = centerY,
                endY = centerY + (barHeight * 0.48f)
            ),
            topLeft = Offset(x, centerY + 2f),
            size = Size(barWidth, barHeight * 0.48f),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        // Glowing Peak Floating Dot
        if (effectiveAmp > 0.2f) {
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = barWidth * 0.55f,
                center = Offset(x + barWidth / 2f, centerY - barHeight - 4f)
            )
        }
    }
}

/**
 * Draws soft translucent glowing area under the undulating wave path.
 */
private fun DrawScope.drawWaveformFilledAura(
    w: Float,
    h: Float,
    centerY: Float,
    amplitudes: List<Float>,
    phase: Float,
    energy: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val auraPath = Path()
    val steps = 60
    val stepDx = w / steps

    auraPath.moveTo(0f, centerY)

    for (i in 0..steps) {
        val x = i * stepDx
        val normX = i.toFloat() / steps
        val ampIdx = ((normX * amplitudes.size).toInt()).coerceIn(0, amplitudes.size - 1)
        val amp = amplitudes.getOrElse(ampIdx) { 0.2f }

        // Windowing function (Hanning curve so wave tapers gracefully at screen edges)
        val window = sin(normX * PI).toFloat()

        val wave = (
                sin(normX * 3.5 * PI + phase).toFloat() * 0.6f +
                        sin(normX * 7.0 * PI - phase * 1.4).toFloat() * 0.3f +
                        cos(normX * 1.8 * PI + phase * 0.8).toFloat() * 0.2f
                )

        val maxAmpHeight = h * 0.32f
        val yOffset = wave * window * (maxAmpHeight * (0.25f + amp * 0.75f) * energy)
        val y = centerY - yOffset

        auraPath.lineTo(x, y)
    }

    auraPath.lineTo(w, centerY)
    auraPath.close()

    drawPath(
        path = auraPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.35f),
                secondaryColor.copy(alpha = 0.15f),
                Color.Transparent
            ),
            startY = centerY - (h * 0.32f * energy),
            endY = centerY
        ),
        style = Fill
    )
}

/**
 * Draws primary continuous glowing spline waveform curve.
 */
private fun DrawScope.drawPrimaryWaveCurve(
    w: Float,
    h: Float,
    centerY: Float,
    amplitudes: List<Float>,
    phase: Float,
    energy: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val wavePath = Path()
    val steps = 80
    val stepDx = w / steps

    for (i in 0..steps) {
        val x = i * stepDx
        val normX = i.toFloat() / steps
        val ampIdx = ((normX * amplitudes.size).toInt()).coerceIn(0, amplitudes.size - 1)
        val amp = amplitudes.getOrElse(ampIdx) { 0.2f }

        val window = sin(normX * PI).toFloat()
        val wave = (
                sin(normX * 3.5 * PI + phase).toFloat() * 0.65f +
                        sin(normX * 7.0 * PI - phase * 1.4).toFloat() * 0.35f
                )

        val maxAmpHeight = h * 0.35f
        val yOffset = wave * window * (maxAmpHeight * (0.25f + amp * 0.75f) * energy)
        val y = centerY - yOffset

        if (i == 0) {
            wavePath.moveTo(x, y)
        } else {
            wavePath.lineTo(x, y)
        }
    }

    // Draw Glowing Backing Stroke
    drawPath(
        path = wavePath,
        brush = Brush.horizontalGradient(
            listOf(
                secondaryColor.copy(alpha = 0.5f),
                primaryColor.copy(alpha = 0.9f),
                EditorialIceBlue,
                primaryColor.copy(alpha = 0.9f),
                secondaryColor.copy(alpha = 0.5f)
            )
        ),
        style = Stroke(
            width = 5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Inner Core White Hot Highlight
    drawPath(
        path = wavePath,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Draws secondary harmonic out-of-phase waveform curve.
 */
private fun DrawScope.drawSecondaryWaveCurve(
    w: Float,
    h: Float,
    centerY: Float,
    amplitudes: List<Float>,
    fastPhase: Float,
    energy: Float,
    secondaryColor: Color
) {
    val crossPath = Path()
    val steps = 60
    val stepDx = w / steps

    for (i in 0..steps) {
        val x = i * stepDx
        val normX = i.toFloat() / steps
        val ampIdx = ((normX * amplitudes.size).toInt()).coerceIn(0, amplitudes.size - 1)
        val amp = amplitudes.getOrElse(ampIdx) { 0.2f }

        val window = sin(normX * PI).toFloat()
        val wave = cos(normX * 5.0 * PI - fastPhase).toFloat() * 0.7f +
                sin(normX * 2.2 * PI + fastPhase * 0.5).toFloat() * 0.3f

        val maxAmpHeight = h * 0.26f
        val yOffset = wave * window * (maxAmpHeight * (0.2f + amp * 0.8f) * energy)
        val y = centerY + yOffset * 0.85f // Inverted phase direction

        if (i == 0) crossPath.moveTo(x, y) else crossPath.lineTo(x, y)
    }

    drawPath(
        path = crossPath,
        brush = Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                secondaryColor.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.6f),
                secondaryColor.copy(alpha = 0.7f),
                Color.Transparent
            )
        ),
        style = Stroke(
            width = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Draws inverted mirror reflection below baseline with lower opacity.
 */
private fun DrawScope.drawMirroredWaveform(
    w: Float,
    h: Float,
    centerY: Float,
    amplitudes: List<Float>,
    phase: Float,
    energy: Float,
    primaryColor: Color
) {
    val mirrorPath = Path()
    val steps = 50
    val stepDx = w / steps

    for (i in 0..steps) {
        val x = i * stepDx
        val normX = i.toFloat() / steps
        val ampIdx = ((normX * amplitudes.size).toInt()).coerceIn(0, amplitudes.size - 1)
        val amp = amplitudes.getOrElse(ampIdx) { 0.2f }

        val window = sin(normX * PI).toFloat()
        val wave = sin(normX * 3.5 * PI + phase).toFloat() * 0.65f

        val maxAmpHeight = h * 0.20f
        val yOffset = wave * window * (maxAmpHeight * (0.2f + amp * 0.8f) * energy)
        val y = centerY + yOffset // Mirrored below baseline

        if (i == 0) mirrorPath.moveTo(x, y) else mirrorPath.lineTo(x, y)
    }

    drawPath(
        path = mirrorPath,
        color = primaryColor.copy(alpha = 0.25f),
        style = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

/**
 * Draws floating kinetic energy spark nodes that ride the crests of the waveform.
 */
private fun DrawScope.drawEnergyParticles(
    w: Float,
    h: Float,
    centerY: Float,
    energy: Float,
    phase: Float,
    primaryColor: Color
) {
    val particleCount = 18
    for (p in 0 until particleCount) {
        val progress = (phase / (2f * PI) + p.toFloat() / particleCount) % 1.0f
        val normX = progress.toFloat()
        val x = normX * w

        val window = sin(normX * PI).toFloat()
        val wave = sin(normX * 3.5 * PI + phase).toFloat() * 0.65f +
                sin(normX * 7.0 * PI - phase * 1.4).toFloat() * 0.35f

        val yOffset = wave * window * (h * 0.35f * energy)
        val y = centerY - yOffset - (sin(normX * 8f * PI).toFloat() * 6f)

        val particleRadius = (2.2f + energy * 3.5f).coerceIn(1.5f, 6.0f)
        val alpha = (window * (0.4f + energy * 0.6f)).coerceIn(0.1f, 1.0f)

        drawCircle(
            color = if (p % 2 == 0) Color.White.copy(alpha = alpha) else primaryColor.copy(alpha = alpha),
            radius = particleRadius,
            center = Offset(x, y)
        )
    }
}
