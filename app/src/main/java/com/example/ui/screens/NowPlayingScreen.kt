package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.data.model.VisualizerMode
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.DynamicWaveformVisualizer
import com.example.ui.components.LyricsView
import com.example.ui.components.SongArtwork
import com.example.ui.components.SpectrumVisualizer
import com.example.ui.components.VinylDiscView
import com.example.ui.components.WaveformScrubber
import com.example.ui.theme.EditorialActive
import com.example.ui.theme.EditorialContainer
import com.example.ui.theme.EditorialDark
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.EditorialIceBlueActive
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
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    visualizerMode: VisualizerMode,
    spectrumAmplitudes: List<Float>,
    waveformEnergy: Float,
    currentLyricIndex: Int,
    repeatMode: RepeatMode,
    isShuffle: Boolean,
    sleepTimerMinutes: Int?,
    queue: List<Song>,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSelectVisualizerMode: (VisualizerMode) -> Unit,
    onCollapse: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onSetSleepTimer: (Int?) -> Unit,
    onPlayTrackFromQueue: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var isQueueSheetOpen by remember { mutableStateOf(false) }
    var isSleepTimerDialogOpen by remember { mutableStateOf(false) }

    AmbientGlowBackground(
        primaryColor = song.primaryColor,
        secondaryColor = song.secondaryColor,
        waveformEnergy = waveformEnergy,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Editorial Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33000000))
                        .clip(CircleShape)
                        .background(GlassTokens.GlassSurfaceUltraThin)
                        .border(1.dp, GlassTokens.GlassBorderSpecular, CircleShape)
                        .clickable { onCollapse() }
                        .testTag("collapse_player_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextEditorialBody,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM PLAYLIST",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = EditorialIceBlue.copy(alpha = 0.85f),
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 13.sp,
                        color = TextEditorialBody,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(
                        onClick = { isSleepTimerDialogOpen = true },
                        modifier = Modifier.size(44.dp).testTag("sleep_timer_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Nightlight,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerMinutes != null) SunsetGold else EditorialIceBlue.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier.size(44.dp).testTag("eq_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = "Equalizer",
                            tint = EditorialIceBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Visualizer Mode Selector Chips (Glassmorphic Pill Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .glassmorphicCard(
                        shape = RoundedCornerShape(22.dp),
                        backgroundBrush = GlassTokens.GlassSurfaceUltraThin,
                        elevation = 4.dp
                    )
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VisualizerMode.values().forEach { mode ->
                        val isSelected = visualizerMode == mode
                        Box(
                            modifier = Modifier
                                .glassmorphicPill(
                                    shape = RoundedCornerShape(16.dp),
                                    isActive = isSelected,
                                    borderWidth = 1.dp
                                )
                                .clickable { onSelectVisualizerMode(mode) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("viz_mode_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EditorialIceBlue else TextMuted
                            )
                        }
                    }
                }
            }

            // Center Stage: Visualizer / Vinyl / Waveform with Swipe-To-Skip Gesture
            val coroutineScope = rememberCoroutineScope()
            val stageOffsetX = remember { Animatable(0f) }
            val density = LocalDensity.current
            val swipeThresholdPx = with(density) { 90.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .aspectRatio(1f)
                        .offset { IntOffset(stageOffsetX.value.roundToInt(), 0) }
                        .pointerInput(song.id) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val current = stageOffsetX.value
                                        if (current < -swipeThresholdPx) {
                                            stageOffsetX.animateTo(
                                                targetValue = -size.width.toFloat(),
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                            onNext()
                                            stageOffsetX.snapTo(size.width.toFloat() * 0.4f)
                                            stageOffsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                        } else if (current > swipeThresholdPx) {
                                            stageOffsetX.animateTo(
                                                targetValue = size.width.toFloat(),
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                            onPrevious()
                                            stageOffsetX.snapTo(-size.width.toFloat() * 0.4f)
                                            stageOffsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                        } else {
                                            stageOffsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        stageOffsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        val newOffset = stageOffsetX.value + dragAmount * 0.85f
                                        stageOffsetX.snapTo(newOffset.coerceIn(-400f, 400f))
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = visualizerMode,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "CenterVisualizerTransition"
                    ) { mode ->
                        when (mode) {
                            VisualizerMode.VINYL -> {
                                VinylDiscView(
                                    song = song,
                                    isPlaying = isPlaying,
                                    is45Rpm = false,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            VisualizerMode.WAVEFORM -> {
                                DynamicWaveformVisualizer(
                                    amplitudes = spectrumAmplitudes,
                                    waveformEnergy = waveformEnergy,
                                    song = song,
                                    isPlaying = isPlaying,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            VisualizerMode.SPECTRUM, VisualizerMode.LIQUID_ORB, VisualizerMode.AURORA -> {
                                SpectrumVisualizer(
                                    mode = mode,
                                    amplitudes = spectrumAmplitudes,
                                    waveformEnergy = waveformEnergy,
                                    song = song,
                                    isPlaying = isPlaying,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            VisualizerMode.LYRICS -> {
                                LyricsView(
                                    song = song,
                                    currentPositionMs = currentPositionMs,
                                    currentLyricIndex = currentLyricIndex,
                                    onSeekToLyric = onSeek,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Interactive Swipe Indicator Cue Badges
                if (abs(stageOffsetX.value) > 25f) {
                    val isNextSwipe = stageOffsetX.value < 0
                    Box(
                        modifier = Modifier
                            .align(if (isNextSwipe) Alignment.CenterEnd else Alignment.CenterStart)
                            .padding(horizontal = 20.dp)
                            .glassmorphicPill(
                                shape = RoundedCornerShape(20.dp),
                                isActive = true,
                                borderWidth = 1.dp
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .alpha((abs(stageOffsetX.value) / swipeThresholdPx).coerceIn(0f, 1f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isNextSwipe) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = EditorialIceBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PREVIOUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EditorialIceBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "NEXT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EditorialIceBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next",
                                    tint = EditorialIceBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Editorial Track Details & Favorite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 17.sp,
                        color = EditorialIceBlue,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_favorite_button")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) HotPink else EditorialIceBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Waveform Scrubber Seekbar
            WaveformScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                primaryColor = song.primaryColor,
                secondaryColor = song.secondaryColor,
                onSeek = onSeek,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Primary Editorial Playback Transport Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.size(48.dp).testTag("player_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) EditorialIceBlue else EditorialIceBlue.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(52.dp).testTag("player_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Glass Play/Pause Squircle Play Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = EditorialIceBlue.copy(alpha = 0.6f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    EditorialIceBlue,
                                    Color(0xFFBACDE5)
                                )
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                        .clickable { onTogglePlay() }
                        .testTag("player_main_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = EditorialDark,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(52.dp).testTag("player_main_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.size(48.dp).testTag("player_repeat_button")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Filled.RepeatOne
                            RepeatMode.ALL -> Icons.Filled.Repeat
                            RepeatMode.OFF -> Icons.Filled.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) EditorialIceBlue else EditorialIceBlue.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Frosted Glass Footer Device Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp)
                    .glassmorphicCard(
                        shape = RoundedCornerShape(16.dp),
                        backgroundBrush = GlassTokens.GlassSurfaceUltraThin,
                        elevation = 4.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Devices,
                            contentDescription = "Devices",
                            tint = EditorialIceBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVING ROOM SPEAKERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = EditorialIceBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    IconButton(
                        onClick = { isQueueSheetOpen = true },
                        modifier = Modifier.size(32.dp).testTag("player_queue_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Frosted Glass Queue Bottom Sheet
    if (isQueueSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isQueueSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xF21C2029),
            modifier = Modifier.testTag("queue_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playing Queue (${queue.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { isQueueSheetOpen = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    itemsIndexed(queue) { index, item ->
                        val isCurrent = item.id == song.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isCurrent) {
                                        Modifier.glassmorphicActive(
                                            shape = RoundedCornerShape(14.dp),
                                            glowColor = EditorialIceBlue,
                                            elevation = 8.dp
                                        )
                                    } else {
                                        Modifier.glassmorphicCard(
                                            shape = RoundedCornerShape(14.dp),
                                            backgroundBrush = GlassTokens.GlassSurfaceUltraThin,
                                            borderBrush = GlassTokens.GlassBorderSubtle,
                                            elevation = 2.dp
                                        )
                                    }
                                )
                                .clickable {
                                    onPlayTrackFromQueue(item)
                                    isQueueSheetOpen = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent) EditorialIceBlue else TextMuted,
                                modifier = Modifier.width(24.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (isCurrent) GlassTokens.GlassBorderNeon else GlassTokens.GlassBorderSpecular,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                SongArtwork(
                                    song = item,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) EditorialIceBlue else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isCurrent && isPlaying) {
                                Icon(
                                    imageVector = Icons.Filled.GraphicEq,
                                    contentDescription = "Playing",
                                    tint = EditorialIceBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Frosted Glass Sleep Timer Modal
    if (isSleepTimerDialogOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSleepTimerDialogOpen = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xF21C2029),
            modifier = Modifier.testTag("sleep_timer_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Editorial Sleep Timer 🌙",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (sleepTimerMinutes != null) "Active: stops in $sleepTimerMinutes minutes" else "Automatically pause audio when you fall asleep",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                listOf(15, 30, 45, 60, null).forEach { option ->
                    val label = if (option == null) "Turn Off Timer" else "$option Minutes"
                    val isSelected = (option != null && sleepTimerMinutes == option) || (option == null && sleepTimerMinutes == null)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.glassmorphicActive(
                                        shape = RoundedCornerShape(14.dp),
                                        glowColor = EditorialIceBlue,
                                        elevation = 8.dp
                                    )
                                } else {
                                    Modifier.glassmorphicCard(
                                        shape = RoundedCornerShape(14.dp),
                                        backgroundBrush = GlassTokens.GlassSurfaceUltraThin,
                                        borderBrush = GlassTokens.GlassBorderSubtle,
                                        elevation = 2.dp
                                    )
                                }
                            )
                            .clickable {
                                onSetSleepTimer(option)
                                isSleepTimerDialogOpen = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) EditorialIceBlue else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = EditorialIceBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
