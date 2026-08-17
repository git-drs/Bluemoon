package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.theme.EditorialDark
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.SonoraTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MusicPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonoraTheme {
                SonoraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SonoraApp(viewModel: MusicPlayerViewModel) {
    val songs by viewModel.songs.collectAsState()
    val filteredSongs by viewModel.filteredSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val moods by viewModel.moods.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val visualizerMode by viewModel.visualizerMode.collectAsState()
    val spectrumAmplitudes by viewModel.spectrumAmplitudes.collectAsState()
    val waveformEnergy by viewModel.waveformEnergy.collectAsState()
    val currentLyricIndex by viewModel.currentLyricIndex.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val equalizerState by viewModel.equalizerState.collectAsState()
    val selectedMoodId by viewModel.selectedMoodId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val availableFolders by viewModel.availableFolders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    // Global ambient glow colors based on current track or default aesthetic palette
    val ambientPrimary = currentSong?.primaryColor ?: Color(0xFF3D4758)
    val ambientSecondary = currentSong?.secondaryColor ?: Color(0xFF1E2638)

    AmbientGlowBackground(
        primaryColor = ambientPrimary,
        secondaryColor = ambientSecondary,
        waveformEnergy = if (isPlaying) waveformEnergy else 0.08f,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Floating Glassmorphic Mini Player Bar
                    MiniPlayerBar(
                        song = currentSong,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onNext = { viewModel.nextTrack() },
                        onPrevious = { viewModel.previousTrack() },
                        onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it.id) } },
                        onExpand = { viewModel.setNowPlayingExpanded(true) }
                    )

                    // Frosted Glassmorphic Navigation Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                spotColor = Color(0x60000000)
                            )
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(GlassTokens.GlassSurfaceFloatingDock)
                            .border(
                                width = 1.dp,
                                brush = GlassTokens.GlassBorderSpecular,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bottom_nav_bar")
                        ) {
                            val items = listOf(
                                Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
                                Triple("Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
                                Triple("Equalizer", Icons.Filled.Equalizer, Icons.Outlined.Equalizer)
                            )

                            items.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
                                val isSelected = selectedTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTab(index) },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                            contentDescription = label,
                                            tint = if (isSelected) EditorialIceBlue else TextMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) EditorialIceBlue else TextMuted
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = EditorialIceBlue.copy(alpha = 0.18f)
                                    ),
                                    modifier = Modifier.testTag("nav_item_${label.lowercase()}")
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> {
                        HomeScreen(
                            songs = filteredSongs,
                            playlists = playlists,
                            moods = moods,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            selectedMoodId = selectedMoodId,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.updateSearchQuery(it) },
                            onMoodSelect = { viewModel.setMoodFilter(it) },
                            onPlaySong = { song, list -> viewModel.playTrack(song, list) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onPlaylistClick = { pl ->
                                val matching = songs.filter { s -> pl.songIds.contains(s.id) }
                                if (matching.isNotEmpty()) {
                                    viewModel.playTrack(matching.first(), matching)
                                }
                            }
                        )
                    }
                    1 -> {
                        LibraryScreen(
                            songs = songs,
                            playlists = playlists,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            isScanning = isScanning,
                            statusMessage = statusMessage,
                            availableFolders = availableFolders,
                            selectedFolder = selectedFolder,
                            onSelectFolder = { viewModel.setSelectedFolder(it) },
                            onPlaySong = { song, list -> viewModel.playTrack(song, list) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onCreatePlaylist = { name, desc, colors -> viewModel.createPlaylist(name, desc, colors) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) },
                            onImportAudioFiles = { viewModel.importAudioFiles(it) },
                            onScanDeviceAudio = { viewModel.scanDeviceAudio() },
                            onImportLrcForCurrentSong = { viewModel.importLrcFileForCurrentSong(it) }
                        )
                    }
                    2 -> {
                        EqualizerScreen(
                            equalizerState = equalizerState,
                            onBandChange = { band, gain -> viewModel.updateEqualizerBand(band, gain) },
                            onBassBoostChange = { viewModel.setBassBoost(it) },
                            onSpatialAudioChange = { viewModel.setSpatialAudio(it) },
                            onReverbChange = { viewModel.setReverbPreset(it) },
                            onPresetSelect = { viewModel.applyEqPreset(it) },
                            onToggleNightcore = { viewModel.toggleNightcore() },
                            onToggleDaycore = { viewModel.toggleDaycore() }
                        )
                    }
                }
            }
        }

        // Full Screen Now Playing Layer (Animated Expansion)
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                song = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                visualizerMode = visualizerMode,
                spectrumAmplitudes = spectrumAmplitudes,
                waveformEnergy = waveformEnergy,
                currentLyricIndex = currentLyricIndex,
                repeatMode = repeatMode,
                isShuffle = isShuffle,
                sleepTimerMinutes = sleepTimerMinutes,
                queue = queue,
                onTogglePlay = { viewModel.togglePlayPause() },
                onNext = { viewModel.nextTrack() },
                onPrevious = { viewModel.previousTrack() },
                onSeek = { viewModel.seekTo(it) },
                onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it.id) } },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onSelectVisualizerMode = { viewModel.setVisualizerMode(it) },
                onCollapse = { viewModel.setNowPlayingExpanded(false) },
                onOpenEqualizer = {
                    viewModel.setNowPlayingExpanded(false)
                    viewModel.selectTab(2)
                },
                onSetSleepTimer = { viewModel.setSleepTimer(it) },
                onPlayTrackFromQueue = { viewModel.playTrack(it) }
            )
        }
    }
}
