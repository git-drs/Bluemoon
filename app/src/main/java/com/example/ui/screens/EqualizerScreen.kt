package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerState
import com.example.data.model.ReverbPreset
import com.example.ui.theme.EditorialActive
import com.example.ui.theme.EditorialContainer
import com.example.ui.theme.EditorialDark
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.EditorialSurface
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.HotPink
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextEditorialBody
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.glassmorphicActive
import com.example.ui.theme.glassmorphicCard
import com.example.ui.theme.glassmorphicPill

@Composable
fun EqualizerScreen(
    equalizerState: EqualizerState,
    onBandChange: (Int, Float) -> Unit,
    onBassBoostChange: (Float) -> Unit,
    onSpatialAudioChange: (Float) -> Unit,
    onReverbChange: (ReverbPreset) -> Unit,
    onPresetSelect: (String) -> Unit,
    onToggleNightcore: () -> Unit,
    onToggleDaycore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandDescriptions = listOf("Sub-Bass", "Warmth", "Presence", "Clarity", "Air")
    val presets = listOf("Balanced Vibe", "Bass Boost", "Vocal Clarity", "Lo-Fi Chill", "Neon Synth", "Acoustic", "Electronic")

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 120.dp)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("equalizer_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Acoustic Tuning",
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
                Text(
                    text = "32-bit Floating Point DSP Engine",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditorialIceBlue.copy(alpha = 0.85f)
                )
            }

            Box(
                modifier = Modifier
                    .glassmorphicPill(
                        shape = RoundedCornerShape(12.dp),
                        isActive = true,
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = equalizerState.presetName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = EditorialIceBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Preset Chips
        Text(
            text = "Master Presets",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { preset ->
                val isSelected = equalizerState.presetName == preset
                Box(
                    modifier = Modifier
                        .glassmorphicPill(
                            shape = RoundedCornerShape(16.dp),
                            isActive = isSelected,
                            borderWidth = 1.dp
                        )
                        .clickable { onPresetSelect(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("eq_preset_$preset")
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) EditorialIceBlue else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5-Band EQ Visual Curve & Sliders Frosted Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphicCard(
                    shape = RoundedCornerShape(24.dp),
                    borderBrush = GlassTokens.GlassBorderSpecular,
                    elevation = 14.dp
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "5-Band Graphic Equalizer",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "±10 dB Range",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Curve Canvas on dark glass
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xCC13151A))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                ) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f

                    // Center 0 dB guide line
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 1.5f
                    )

                    val path = Path()
                    val bandCount = equalizerState.bands.size
                    for (i in 0 until bandCount) {
                        val gain = equalizerState.bands[i].coerceIn(-10f, 10f)
                        val x = (i.toFloat() / (bandCount - 1).toFloat()) * w
                        val y = midY - (gain / 10f) * (midY * 0.8f)

                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                        // Glowing point
                        drawCircle(
                            color = EditorialIceBlue,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(listOf(EditorialIceBlue, Color(0xFFBACDE5), EditorialIceBlue)),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Band Sliders
                equalizerState.bands.forEachIndexed { index, gain ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${bandLabels[index]} (${bandDescriptions[index]})",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = String.format("%+.1f dB", gain),
                                style = MaterialTheme.typography.labelSmall,
                                color = EditorialIceBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = gain,
                            onValueChange = { onBandChange(index, it) },
                            valueRange = -10f..10f,
                            colors = SliderDefaults.colors(
                                thumbColor = EditorialIceBlue,
                                activeTrackColor = EditorialIceBlue,
                                inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag("eq_slider_band_$index")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bass Boost & Spatial Audio Dials
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bass Boost Glass Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .glassmorphicCard(
                        shape = RoundedCornerShape(22.dp),
                        elevation = 10.dp
                    )
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape, spotColor = EditorialIceBlue.copy(alpha = 0.3f))
                            .clip(CircleShape)
                            .background(EditorialIceBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = null,
                            tint = EditorialIceBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bass Boost",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(equalizerState.bassBoost * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = EditorialIceBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Slider(
                        value = equalizerState.bassBoost,
                        onValueChange = onBassBoostChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = EditorialIceBlue,
                            activeTrackColor = EditorialIceBlue,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("bass_boost_slider")
                    )
                }
            }

            // Spatial Audio 3D Glass Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .glassmorphicCard(
                        shape = RoundedCornerShape(22.dp),
                        elevation = 10.dp
                    )
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape, spotColor = EditorialIceBlue.copy(alpha = 0.3f))
                            .clip(CircleShape)
                            .background(EditorialIceBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SurroundSound,
                            contentDescription = null,
                            tint = EditorialIceBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Spatial 3D",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(equalizerState.spatialAudio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = EditorialIceBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Slider(
                        value = equalizerState.spatialAudio,
                        onValueChange = onSpatialAudioChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = EditorialIceBlue,
                            activeTrackColor = EditorialIceBlue,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("spatial_audio_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sound Stage Reverb Presets Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphicCard(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 10.dp
                )
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "Acoustic Environment / Reverb",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReverbPreset.values().forEach { reverb ->
                        val isSelected = equalizerState.reverbPreset == reverb
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .glassmorphicPill(
                                    shape = RoundedCornerShape(12.dp),
                                    isActive = isSelected,
                                    borderWidth = 1.dp
                                )
                                .clickable { onReverbChange(reverb) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = reverb.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isSelected) EditorialIceBlue else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Aesthetic Remix Toggles (Nightcore & Daycore) Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphicCard(
                    shape = RoundedCornerShape(22.dp),
                    borderBrush = GlassTokens.GlassBorderSpecular,
                    elevation = 10.dp
                )
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "Aesthetic Playback Modes",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Nightcore Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(6.dp, CircleShape, spotColor = SunsetGold.copy(alpha = 0.4f))
                                .clip(CircleShape)
                                .background(SunsetGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Nightcore Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "1.25x Speed + High-Frequency Pitch Lift",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = equalizerState.isNightcore,
                        onCheckedChange = { onToggleNightcore() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EditorialDark,
                            checkedTrackColor = SunsetGold
                        ),
                        modifier = Modifier.testTag("nightcore_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daycore Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(6.dp, CircleShape, spotColor = EditorialIceBlue.copy(alpha = 0.4f))
                                .clip(CircleShape)
                                .background(EditorialIceBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Nightlight, contentDescription = null, tint = EditorialIceBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daycore / Slowed + Reverb",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "0.8x Speed + Cosmic Space Reverb Decay",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = equalizerState.isDaycore,
                        onCheckedChange = { onToggleDaycore() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EditorialDark,
                            checkedTrackColor = EditorialIceBlue
                        ),
                        modifier = Modifier.testTag("daycore_switch")
                    )
                }
            }
        }
    }
}
