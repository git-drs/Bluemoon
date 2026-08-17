package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EditorialActive
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun WaveformScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    primaryColor: Color,
    secondaryColor: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val safeDuration = durationMs.coerceAtLeast(1000L)
    val actualFraction = (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    val displayFraction = if (isDragging) dragFraction else actualFraction

    val displayedPosMs = (displayFraction * safeDuration).toLong()
    val remainingMs = (safeDuration - displayedPosMs).coerceAtLeast(0L)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("waveform_scrubber")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeek((newFraction * safeDuration).toLong())
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            dragFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragFraction * safeDuration).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                val w = size.width
                val h = size.height
                val numBars = 44
                val barSpacing = w / numBars
                val barWidth = barSpacing * 0.52f

                // Draw Waveform bars with sleek editorial finish
                for (i in 0 until numBars) {
                    val barFrac = i.toFloat() / numBars
                    val x = i * barSpacing + (barSpacing - barWidth) / 2f

                    // Harmonious subtle audio envelope
                    val waveAmp = (sin(i * 0.38).toFloat() * 0.35f + sin(i * 0.85).toFloat() * 0.35f + 0.55f).coerceIn(0.25f, 1.0f)
                    val barHeight = (h * 0.68f) * waveAmp
                    val y = (h - barHeight) / 2f

                    val isPlayed = barFrac <= displayFraction

                    if (isPlayed) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(EditorialIceBlue, primaryColor.copy(alpha = 0.9f)),
                                startY = y,
                                endY = y + barHeight
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    } else {
                        drawRoundRect(
                            color = EditorialActive.copy(alpha = 0.65f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }

                // Sleek Glowing Seek Handle
                val handleX = w * displayFraction
                val handleRadius = if (isDragging) 7.dp.toPx() else 5.5.dp.toPx()

                // Subtle Outer Glow
                drawCircle(
                    color = EditorialIceBlue.copy(alpha = 0.3f),
                    radius = handleRadius * 2.2f,
                    center = Offset(handleX, h / 2f)
                )

                // Handle Core
                drawCircle(
                    color = EditorialIceBlue,
                    radius = handleRadius,
                    center = Offset(handleX, h / 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Time Labels with clean editorial font & tracking
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(displayedPosMs),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDragging) EditorialIceBlue else TextMuted,
                letterSpacing = 0.5.sp
            )
            Text(
                text = formatDuration(safeDuration),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000L
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}

