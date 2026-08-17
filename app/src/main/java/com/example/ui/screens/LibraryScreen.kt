package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.components.SongArtwork
import com.example.ui.components.formatDuration
import com.example.ui.theme.EditorialContainer
import com.example.ui.theme.EditorialDark
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.EditorialSurface
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.glassmorphicActive
import com.example.ui.theme.glassmorphicCard
import com.example.ui.theme.glassmorphicPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    currentSong: Song?,
    isPlaying: Boolean,
    isScanning: Boolean,
    statusMessage: String?,
    availableFolders: List<String>,
    selectedFolder: String?,
    onSelectFolder: (String?) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onCreatePlaylist: (String, String, List<Color>) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onImportAudioFiles: (List<Uri>) -> Unit,
    onScanDeviceAudio: () -> Unit,
    onImportLrcForCurrentSong: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLibraryTab by remember { mutableIntStateOf(0) }
    var isCreatePlaylistDialogOpen by remember { mutableStateOf(false) }
    var selectedSongForInfo by remember { mutableStateOf<Song?>(null) }

    // Multi-file audio picker for batch import
    val multiFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) {
            onImportAudioFiles(uris)
        }
    }

    // LRC synced lyrics picker
    val lrcPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportLrcForCurrentSong(uri)
        }
    }

    // Permission launcher for media audio scan
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onScanDeviceAudio()
        }
    }

    fun requestScanPermissionAndRun() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val currentPermission = ContextCompat.checkSelfPermission(context, permission)
        if (currentPermission == PackageManager.PERMISSION_GRANTED) {
            onScanDeviceAudio()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 120.dp)
            .testTag("library_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Acoustic Library",
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    color = TextPrimary
                )
                Text(
                    text = "${songs.size} high-resolution tracks stored",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditorialIceBlue.copy(alpha = 0.85f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(end = 8.dp),
                        color = EditorialIceBlue,
                        strokeWidth = 2.5.dp
                    )
                }

                // Glass Add Playlist Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(8.dp, CircleShape, spotColor = EditorialIceBlue.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(GlassTokens.GlassSurfaceUltraThin)
                        .border(1.dp, GlassTokens.GlassBorderSpecular, CircleShape)
                        .clickable { isCreatePlaylistDialogOpen = true }
                        .testTag("create_playlist_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New Playlist",
                        tint = EditorialIceBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Status Toast Banner
        if (!statusMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .glassmorphicPill(
                        shape = RoundedCornerShape(14.dp),
                        isActive = true,
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AudioFile, contentDescription = null, tint = EditorialIceBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EditorialIceBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Frosted Glass Tabs
        val tabs = listOf("Tracks", "Playlists", "Favorites", "Storage")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .glassmorphicCard(
                    shape = RoundedCornerShape(18.dp),
                    backgroundBrush = GlassTokens.GlassSurfaceUltraThin,
                    elevation = 4.dp
                )
                .padding(4.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedLibraryTab,
                containerColor = Color.Transparent,
                contentColor = EditorialIceBlue,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedLibraryTab]),
                        color = EditorialIceBlue,
                        height = 2.5.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedLibraryTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedLibraryTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EditorialIceBlue else TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedLibraryTab) {
            0 -> {
                // All Tracks
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(songs) { index, song ->
                        TrackRowItem(
                            song = song,
                            index = index + 1,
                            isCurrent = currentSong?.id == song.id,
                            isPlaying = isPlaying,
                            onClick = { onPlaySong(song, songs) },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            onInfoClick = { selectedSongForInfo = song }
                        )
                    }
                }
            }
            1 -> {
                // Playlists
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(playlists) { playlist ->
                        val matchingSongs = songs.filter { s -> playlist.songIds.contains(s.id) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphicCard(
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = 8.dp
                                )
                                .clickable {
                                    if (matchingSongs.isNotEmpty()) {
                                        onPlaySong(matchingSongs.first(), matchingSongs)
                                    }
                                }
                                .padding(16.dp)
                                .testTag("library_playlist_item_${playlist.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = EditorialIceBlue.copy(alpha = 0.3f))
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0x55D0E4FF),
                                                    Color(0x223D4758)
                                                )
                                            )
                                        )
                                        .border(1.dp, GlassTokens.GlassBorderSpecular, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = EditorialIceBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${matchingSongs.size} tracks • ${playlist.description}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (playlist.isCustom) {
                                    IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete Playlist",
                                            tint = HotPink.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Favorites
                val favoriteSongs = songs.filter { it.isFavorite }
                if (favoriteSongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = HotPink.copy(alpha = 0.4f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No favorite tracks yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the heart icon on any song to save favorites here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(favoriteSongs) { index, song ->
                            TrackRowItem(
                                song = song,
                                index = index + 1,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                onClick = { onPlaySong(song, favoriteSongs) },
                                onToggleFavorite = { onToggleFavorite(song.id) },
                                onInfoClick = { selectedSongForInfo = song }
                            )
                        }
                    }
                }
            }
            3 -> {
                // Folders & Local Audio Storage
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Quick Action Glass Panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphicCard(
                                    shape = RoundedCornerShape(22.dp),
                                    borderBrush = GlassTokens.GlassBorderNeon,
                                    elevation = 12.dp
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Storage, contentDescription = null, tint = EditorialIceBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "High-Fidelity Audio Importer",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Read MP3, FLAC, WAV, AAC, M4A from your storage with full ID3 tag extraction and hardware EQ acceleration.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { multiFilePicker.launch(arrayOf("audio/*")) },
                                        modifier = Modifier.weight(1f).testTag("import_file_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = EditorialIceBlue),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Filled.FileOpen, contentDescription = null, tint = EditorialDark)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pick Audio", color = EditorialDark, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { requestScanPermissionAndRun() },
                                        modifier = Modifier.weight(1f).testTag("scan_device_audio_button"),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialIceBlue.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = EditorialIceBlue)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scan All", color = EditorialIceBlue)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = { lrcPicker.launch(arrayOf("*/*")) },
                                    modifier = Modifier.fillMaxWidth().testTag("import_lrc_button"),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Filled.Lyrics, contentDescription = null, tint = PastelLavender)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import .LRC Lyrics for Current Song", color = PastelLavender)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Storage Folders",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    // Folder filter glass chips
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .glassmorphicPill(
                                        shape = RoundedCornerShape(14.dp),
                                        isActive = selectedFolder == null,
                                        borderWidth = 1.dp
                                    )
                                    .clickable { onSelectFolder(null) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "All Folders",
                                    color = if (selectedFolder == null) EditorialIceBlue else TextPrimary,
                                    fontWeight = if (selectedFolder == null) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            availableFolders.forEach { folder ->
                                val isSelected = selectedFolder == folder
                                Box(
                                    modifier = Modifier
                                        .glassmorphicPill(
                                            shape = RoundedCornerShape(14.dp),
                                            isActive = isSelected,
                                            borderWidth = 1.dp
                                        )
                                        .clickable { onSelectFolder(folder) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Folder,
                                            contentDescription = null,
                                            tint = if (isSelected) EditorialIceBlue else TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = folder,
                                            color = if (isSelected) EditorialIceBlue else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Filtered local tracks
                    val folderSongs = if (selectedFolder != null) songs.filter { it.folderName == selectedFolder } else songs
                    itemsIndexed(folderSongs) { idx, item ->
                        TrackRowItem(
                            song = item,
                            index = idx + 1,
                            isCurrent = currentSong?.id == item.id,
                            isPlaying = isPlaying,
                            onClick = { onPlaySong(item, folderSongs) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onInfoClick = { selectedSongForInfo = item }
                        )
                    }
                }
            }
        }
    }

    // Frosted Glass Song Info Bottom Sheet
    if (selectedSongForInfo != null) {
        val song = selectedSongForInfo!!
        ModalBottomSheet(
            onDismissRequest = { selectedSongForInfo = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xF21C2029),
            modifier = Modifier.testTag("track_info_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = EditorialIceBlue.copy(alpha = 0.3f))
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, GlassTokens.GlassBorderSpecular, RoundedCornerShape(16.dp))
                    ) {
                        SongArtwork(song = song, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EditorialIceBlue
                        )
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AUDIO METADATA & SPECS",
                    style = MaterialTheme.typography.labelSmall,
                    color = EditorialIceBlue,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    "Format" to song.formattedAudioFormat(),
                    "Bitrate" to "${song.bitRateKbps} kbps",
                    "Sample Rate" to "${song.sampleRateHz / 1000.0} kHz",
                    "Duration" to formatDuration(song.durationSeconds * 1000L),
                    "File Size" to song.formattedFileSize(),
                    "Storage Location" to (song.filePath ?: "Procedural Master Stream"),
                    "Folder" to song.folderName,
                    "Synced Lyrics" to "${song.lyrics.size} lines available"
                ).forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = value,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onPlaySong(song, songs)
                        selectedSongForInfo = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialIceBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Play This Track Now", color = EditorialDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Create Playlist Glass Dialog
    if (isCreatePlaylistDialogOpen) {
        var newPlaylistName by remember { mutableStateOf("") }
        var newPlaylistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isCreatePlaylistDialogOpen = false },
            containerColor = Color(0xF2232833),
            title = {
                Text(
                    text = "Create Editorial Playlist",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EditorialIceBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPlaylistDesc,
                        onValueChange = { newPlaylistDesc = it },
                        label = { Text("Description / Theme", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EditorialIceBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(
                                newPlaylistName,
                                if (newPlaylistDesc.isNotBlank()) newPlaylistDesc else "Editorial collection",
                                listOf(EditorialIceBlue, EditorialContainer)
                            )
                            isCreatePlaylistDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialIceBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create", color = EditorialDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreatePlaylistDialogOpen = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun TrackRowItem(
    song: Song,
    index: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                if (isCurrent) {
                    Modifier.glassmorphicActive(
                        shape = RoundedCornerShape(16.dp),
                        glowColor = EditorialIceBlue,
                        elevation = 10.dp
                    )
                } else {
                    Modifier.glassmorphicCard(
                        shape = RoundedCornerShape(16.dp),
                        backgroundBrush = GlassTokens.GlassSurfaceUltraThin,
                        borderBrush = GlassTokens.GlassBorderSubtle,
                        elevation = 2.dp
                    )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent && isPlaying) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = "Playing",
                    tint = EditorialIceBlue,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) EditorialIceBlue else TextMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    1.dp,
                    if (isCurrent) GlassTokens.GlassBorderNeon else GlassTokens.GlassBorderSpecular,
                    RoundedCornerShape(14.dp)
                )
        ) {
            SongArtwork(
                song = song,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) EditorialIceBlue else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isLocal) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(0.5.dp, GlassTokens.GlassBorderNeon, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = song.formattedAudioFormat().take(4),
                            fontSize = 9.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "${song.artist} • ${song.genre}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = formatDuration(song.durationSeconds * 1000L),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (song.isFavorite) HotPink else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        if (onInfoClick != null) {
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Details",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
