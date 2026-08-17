package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoodStation
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.components.SongArtwork
import com.example.ui.components.formatDuration
import com.example.ui.theme.EditorialActive
import com.example.ui.theme.EditorialContainer
import com.example.ui.theme.EditorialDark
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.EditorialIceBlueActive
import com.example.ui.theme.EditorialSurface
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextEditorialBody
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.glassmorphicActive
import com.example.ui.theme.glassmorphicCard
import com.example.ui.theme.glassmorphicPill

@Composable
fun HomeScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    moods: List<MoodStation>,
    currentSong: Song?,
    isPlaying: Boolean,
    selectedMoodId: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onMoodSelect: (String?) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 120.dp)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // App Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BLUEMOON",
                                style = MaterialTheme.typography.displayMedium,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 1.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                EditorialIceBlue,
                                                Color(0xFFBACDE5)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "HI-RES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialDark,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Text(
                            text = "Atmospheric Echoes & Editorial Frequencies",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EditorialIceBlue.copy(alpha = 0.85f)
                        )
                    }

                    // Glass Headphone Orb
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(12.dp, CircleShape, spotColor = EditorialIceBlue.copy(alpha = 0.25f))
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x553D4758),
                                        Color(0x221E222C)
                                    )
                                )
                            )
                            .border(1.dp, GlassTokens.GlassBorderSpecular, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Headphones,
                            contentDescription = "Audio Engine",
                            tint = EditorialIceBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frosted Glass Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33000000))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x3D3D4758),
                                    Color(0x241F232D)
                                )
                            )
                        )
                        .border(1.dp, GlassTokens.GlassBorderSpecular, RoundedCornerShape(20.dp))
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search songs, artists, genres...", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = EditorialIceBlue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_bar"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // Hero Featured Card
        item {
            val featuredSong = songs.firstOrNull()
            if (featuredSong != null && searchQuery.isBlank() && selectedMoodId == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .glassmorphicCard(
                            shape = RoundedCornerShape(26.dp),
                            borderBrush = GlassTokens.GlassBorderActive,
                            backgroundBrush = Brush.linearGradient(
                                listOf(
                                    Color(0x6648556B),
                                    Color(0x40252A36),
                                    Color(0x28181A22)
                                )
                            ),
                            elevation = 20.dp,
                            spotColor = featuredSong.primaryColor.copy(alpha = 0.4f)
                        )
                        .clickable { onPlaySong(featuredSong, songs) }
                        .padding(20.dp)
                        .testTag("hero_featured_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color(0x55000000))
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, GlassTokens.GlassBorderSpecular, RoundedCornerShape(18.dp))
                        ) {
                            SongArtwork(
                                song = featuredSong,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EditorialIceBlue.copy(alpha = 0.18f))
                                    .border(0.5.dp, GlassTokens.GlassBorderNeon, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "FEATURED SELECTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EditorialIceBlue,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 9.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = featuredSong.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${featuredSong.artist} • ${featuredSong.genre}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Glass Play Action Squircle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = EditorialIceBlue.copy(alpha = 0.5f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            EditorialIceBlue,
                                            Color(0xFFBACDE5)
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play Featured",
                                tint = EditorialDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Mood Stations
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Aesthetic Mood Stations",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(moods) { mood ->
                        val isSelected = selectedMoodId == mood.id
                        Box(
                            modifier = Modifier
                                .glassmorphicPill(
                                    shape = RoundedCornerShape(18.dp),
                                    isActive = isSelected,
                                    borderWidth = 1.dp
                                )
                                .clickable { onMoodSelect(mood.id) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("mood_chip_${mood.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = mood.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mood.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EditorialIceBlue else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Curated Playlists
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                Text(
                    text = "Curated Editorial Mixes",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(playlists) { playlist ->
                        Box(
                            modifier = Modifier
                                .width(165.dp)
                                .glassmorphicCard(
                                    shape = RoundedCornerShape(22.dp),
                                    elevation = 12.dp
                                )
                                .clickable { onPlaylistClick(playlist) }
                                .padding(16.dp)
                                .testTag("playlist_card_${playlist.id}")
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = EditorialIceBlue.copy(alpha = 0.3f))
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0x50D0E4FF),
                                                    Color(0x203D4758)
                                                )
                                            )
                                        )
                                        .border(1.dp, GlassTokens.GlassBorderSpecular, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Headphones,
                                        contentDescription = null,
                                        tint = EditorialIceBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = playlist.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Track List Section Header
        item {
            Column(modifier = Modifier.padding(top = 22.dp, start = 20.dp, end = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results" else if (selectedMoodId != null) "Mood Tracks" else "Popular Editorial Tracks",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${songs.size} tracks",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Tracks items with glassmorphic styling
        itemsIndexed(songs) { index, song ->
            val isCurrent = currentSong?.id == song.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .then(
                        if (isCurrent) {
                            Modifier.glassmorphicActive(
                                shape = RoundedCornerShape(18.dp),
                                glowColor = EditorialIceBlue,
                                elevation = 12.dp
                            )
                        } else {
                            Modifier.glassmorphicCard(
                                shape = RoundedCornerShape(18.dp),
                                backgroundBrush = GlassTokens.GlassSurfaceUltraThin,
                                borderBrush = GlassTokens.GlassBorderSubtle,
                                elevation = 2.dp
                            )
                        }
                    )
                    .clickable { onPlaySong(song, songs) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("track_item_${song.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Index / Equalizer icon
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
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) EditorialIceBlue else TextMuted
                        )
                    }
                }

                // Artwork
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

                // Title & Details
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
                    Text(
                        text = "${song.artist} • ${song.genre}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formatDuration(song.durationSeconds * 1000L),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                IconButton(
                    onClick = { onToggleFavorite(song.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) HotPink else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
