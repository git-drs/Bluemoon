package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.audio.AudioFileReader
import com.example.data.model.LyricLine
import com.example.data.model.MoodStation
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.CelestialBlue
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.SunsetGold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class SongRepository {

    private val _songs = MutableStateFlow<List<Song>>(getCuratedSongs())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(getInitialPlaylists())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _moods = MutableStateFlow<List<MoodStation>>(getMoodStations())
    val moods: StateFlow<List<MoodStation>> = _moods.asStateFlow()

    private val _scannedCount = MutableStateFlow(0)
    val scannedCount: StateFlow<Int> = _scannedCount.asStateFlow()

    fun toggleFavorite(songId: String) {
        _songs.update { list ->
            list.map { song ->
                if (song.id == songId) song.copy(isFavorite = !song.isFavorite) else song
            }
        }
    }

    fun addCustomPlaylist(name: String, description: String, colors: List<Color>) {
        val newPlaylist = Playlist(
            id = "playlist_${System.currentTimeMillis()}",
            name = name,
            description = description,
            gradientColors = colors,
            iconName = "music_note",
            songIds = emptyList(),
            isCustom = true
        )
        _playlists.update { it + newPlaylist }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        _playlists.update { list ->
            list.map { playlist ->
                if (playlist.id == playlistId && !playlist.songIds.contains(songId)) {
                    playlist.copy(songIds = playlist.songIds + songId)
                } else playlist
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        _playlists.update { list ->
            list.map { playlist ->
                if (playlist.id == playlistId) {
                    playlist.copy(songIds = playlist.songIds.filter { it != songId })
                } else playlist
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.update { list -> list.filter { it.id != playlistId } }
    }

    suspend fun importAudioUris(context: Context, uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val newSongs = mutableListOf<Song>()
        for (uri in uris) {
            val song = AudioFileReader.readSongFromUri(context, uri)
            if (song != null) {
                newSongs.add(song)
            }
        }
        if (newSongs.isNotEmpty()) {
            _songs.update { current ->
                val existingUris = current.mapNotNull { it.audioUri }.toSet()
                val filtered = newSongs.filterNot { it.audioUri in existingUris }
                filtered + current
            }
        }
        newSongs.size
    }

    suspend fun scanDeviceAudio(context: Context): Int = withContext(Dispatchers.IO) {
        val scanned = AudioFileReader.scanMediaStoreAudio(context)
        if (scanned.isNotEmpty()) {
            _songs.update { current ->
                val existingIds = current.map { s -> s.id }.toSet()
                val existingUris = current.mapNotNull { s -> s.audioUri }.toSet()
                val filtered = scanned.filterNot { it.id in existingIds || (it.audioUri != null && it.audioUri in existingUris) }
                filtered + current
            }
        }
        _scannedCount.value = scanned.size
        scanned.size
    }

    fun attachLyricsToSong(songId: String, lyrics: List<LyricLine>) {
        _songs.update { list ->
            list.map { song ->
                if (song.id == songId) song.copy(lyrics = lyrics) else song
            }
        }
    }

    companion object {
        private fun getCuratedSongs(): List<Song> {
            return listOf(
                Song(
                    id = "track_1_neon_drift",
                    title = "Midnight Neon Drift",
                    artist = "Kavinsky Mirage",
                    album = "Outrun Horizons 2088",
                    durationSeconds = 214,
                    artworkResId = R.drawable.img_album_neon_drift,
                    primaryColor = NeonCyan,
                    secondaryColor = ElectricPurple,
                    genre = "Synthwave / Cyberpunk",
                    bpm = 118,
                    synthScaleType = "SYNTHWAVE_MAJOR",
                    isFavorite = true,
                    bitRateKbps = 320,
                    sampleRateHz = 48000,
                    folderName = "Curated Masters",
                    lyrics = listOf(
                        LyricLine(0, "Streets bathed in cyan glow", "Speeding down the empty neon coast"),
                        LyricLine(8000, "Headlights slicing through the midnight haze", "118 BPM synth pulse"),
                        LyricLine(18000, "Analog synthesizer echoes in the cockpit", "Distant skyline fading behind"),
                        LyricLine(28000, "We chase the phantom frequencies tonight", "Sub-bass vibration fills the room"),
                        LyricLine(42000, "Overdrive the engine, let the bass line roll", "Arpeggiator crescendo"),
                        LyricLine(58000, "No destination, only endless highway lines", "Analog filter sweeping down"),
                        LyricLine(75000, "Echoes of a retro futuristic dream", "Smooth electric solo"),
                        LyricLine(92000, "Vapor trails glowing under purple skies", "Full synth chorus"),
                        LyricLine(115000, "Never looking back into the shadows", "FM Bass breakdown"),
                        LyricLine(135000, "Cruising through the electric twilight", "Spatial harmony"),
                        LyricLine(160000, "We are eternal in this midnight pulse", "Decrescendo into gentle reverb"),
                        LyricLine(190000, "Fade out into starlight...", "Final atmospheric chord")
                    )
                ),
                Song(
                    id = "track_2_cosmic_lofi",
                    title = "Celestial Study Session",
                    artist = "Aura & The Lofi Dreamers",
                    album = "Midnight Constellations",
                    durationSeconds = 178,
                    artworkResId = R.drawable.img_album_cosmic_lofi,
                    primaryColor = PastelLavender,
                    secondaryColor = CyberViolet,
                    genre = "Lo-Fi Chillhop",
                    bpm = 84,
                    synthScaleType = "LOFI_PENTATONIC",
                    isFavorite = true,
                    bitRateKbps = 320,
                    sampleRateHz = 44100,
                    folderName = "Curated Masters",
                    lyrics = listOf(
                        LyricLine(0, "Rain drops gently tap the window pane", "Warm vinyl crackle warming up"),
                        LyricLine(10000, "Steaming mug of lavender tea in hand", "Rhodes electric piano chords"),
                        LyricLine(22000, "Pencil scribbles on ancient parchment paper", "84 BPM boom-bap rhythm"),
                        LyricLine(36000, "The moon whispers secrets to the tired stars", "Soft bass groove enters"),
                        LyricLine(52000, "Every thought floats weightless in the nebula", "Gentle flute melody"),
                        LyricLine(70000, "Deep focus, breathing in peace and serenity", "Relaxing acoustic undertone"),
                        LyricLine(90000, "Clock ticks slow as the universe unwinds", "Warm saturation vibe"),
                        LyricLine(110000, "A sanctuary tucked between time and dreams", "Muted guitar arpeggio"),
                        LyricLine(135000, "Letting go of worries from yesterday", "Smooth Rhodes cadence"),
                        LyricLine(155000, "Soft exhale into peaceful night...", "Vinyl crackle outro")
                    )
                ),
                Song(
                    id = "track_3_midnight_rain",
                    title = "Tokyo Rain Reflections",
                    artist = "Velvet Noir Collective",
                    album = "Shibuya Nights Vol. 3",
                    durationSeconds = 236,
                    artworkResId = R.drawable.img_album_midnight_rain,
                    primaryColor = HotPink,
                    secondaryColor = CelestialBlue,
                    genre = "Neo-Soul / Ambient Jazz",
                    bpm = 92,
                    synthScaleType = "JAZZ_CHILL_DORIAN",
                    isFavorite = false,
                    bitRateKbps = 320,
                    sampleRateHz = 48000,
                    folderName = "Curated Masters",
                    lyrics = listOf(
                        LyricLine(0, "Neon reflections shimmering on wet black asphalt", "Distant thunderstorm ambiance"),
                        LyricLine(12000, "Umbrellas bobbing like lanterns in the alley", "Muted trumpet intro"),
                        LyricLine(26000, "Under the canopy of a quiet ramen stall", "Smooth walking upright bass"),
                        LyricLine(42000, "Warm golden broth and a heart full of memories", "Lush major 7th chords"),
                        LyricLine(62000, "Sirens drift like ghosts across the skyline", "Velvet keys solo"),
                        LyricLine(85000, "Holding on to the warmth of your silhouette", "Emotional saxophone vibrato"),
                        LyricLine(110000, "City lights blurring into watercolor smears", "Subtle brush drums"),
                        LyricLine(138000, "Tomorrow is another world away", "Atmospheric delay trails"),
                        LyricLine(168000, "Lost inside the rhythm of falling raindrops", "Lush vocal harmonies"),
                        LyricLine(198000, "Tokyo sleeps while our thoughts stay awake", "Gentle piano cadence"),
                        LyricLine(220000, "Goodnight to the rainy boulevard...", "Rain soundscape fade")
                    )
                ),
                Song(
                    id = "track_4_solar_echoes",
                    title = "Solaris Resonance",
                    artist = "Orbital Horizon",
                    album = "Singularity Soundscapes",
                    durationSeconds = 250,
                    artworkResId = R.drawable.img_album_solar_echoes,
                    primaryColor = SunsetGold,
                    secondaryColor = CyberViolet,
                    genre = "Cinematic Ambient / 3D Sound",
                    bpm = 70,
                    synthScaleType = "SOLAR_AMBIENT",
                    isFavorite = true,
                    bitRateKbps = 320,
                    sampleRateHz = 96000,
                    folderName = "Curated Masters",
                    lyrics = listOf(
                        LyricLine(0, "Drifting in silent orbit above golden oceans", "Shimmering granular textures"),
                        LyricLine(15000, "Solar flares casting halos across the horizon", "Deep sub-harmonic resonance"),
                        LyricLine(35000, "Time dissolves into pure frequency and light", "Crystalline synthesizer bell"),
                        LyricLine(60000, "Weightless floating through cosmic geometry", "Binaural spatial sweep"),
                        LyricLine(90000, "Every breath aligns with the pulse of stars", "Rich cinematic pad swells"),
                        LyricLine(125000, "Echoes rebounding from quantum dimensions", "Ethereal angelic chorus"),
                        LyricLine(160000, "A golden sphere illuminates infinity", "Harmonic overtone bloom"),
                        LyricLine(195000, "We are the universe experiencing itself", "Warm analog embrace"),
                        LyricLine(230000, "Eternal light, eternal calm...", "Infinite reverb tail")
                    )
                ),
                Song(
                    id = "track_5_cyber_pulse",
                    title = "Hyperdrive Overload",
                    artist = "Glitch Syndicate",
                    album = "Zero Gravity Protocol",
                    durationSeconds = 195,
                    artworkResId = R.drawable.img_album_neon_drift,
                    primaryColor = AcidGreen,
                    secondaryColor = ElectricPurple,
                    genre = "Cyberpunk / Darksynth",
                    bpm = 132,
                    synthScaleType = "CYBERPUNK_DRIVE",
                    isFavorite = false,
                    bitRateKbps = 320,
                    sampleRateHz = 48000,
                    folderName = "Curated Masters",
                    lyrics = listOf(
                        LyricLine(0, "Initialize cybernetic neural link...", "System boot audio sequence"),
                        LyricLine(8000, "Clock cycles overclocked to maximum rate", "Distorted sawtooth bassline"),
                        LyricLine(22000, "Binary storm surging through the fiber grid", "132 BPM heavy kick & snare"),
                        LyricLine(40000, "Neon sparks showering from severed wires", "Screaming synth lead"),
                        LyricLine(60000, "Break the firewall, ignite the thrusters", "Hardcore bass drop"),
                        LyricLine(85000, "Data velocity exceeding terminal speed", "Hyper-speed arpeggios"),
                        LyricLine(110000, "We operate beyond the mainframe limits", "Glitch stutter effect"),
                        LyricLine(135000, "Pure adrenaline in high-voltage flow", "Industrial rhythm clash"),
                        LyricLine(160000, "Overdrive sustained. Victory confirmed.", "Power surge climax"),
                        LyricLine(180000, "System cool down...", "Filter cutoff closing")
                    )
                )
            )
        }

        private fun getInitialPlaylists(): List<Playlist> {
            return listOf(
                Playlist(
                    id = "playlist_favs",
                    name = "Aesthetic Favorites",
                    description = "Your most loved vibes & celestial tracks",
                    gradientColors = listOf(HotPink, CyberViolet),
                    iconName = "favorite",
                    songIds = listOf("track_1_neon_drift", "track_2_cosmic_lofi", "track_4_solar_echoes")
                ),
                Playlist(
                    id = "playlist_cyber_night",
                    name = "Midnight Synthwave Ride",
                    description = "Fast neon highways & analog basslines",
                    gradientColors = listOf(NeonCyan, ElectricPurple),
                    iconName = "speed",
                    songIds = listOf("track_1_neon_drift", "track_5_cyber_pulse")
                ),
                Playlist(
                    id = "playlist_deep_focus",
                    name = "Deep Lo-Fi Space",
                    description = "Cozy beats to code, study and relax to",
                    gradientColors = listOf(PastelLavender, CelestialBlue),
                    iconName = "spa",
                    songIds = listOf("track_2_cosmic_lofi", "track_4_solar_echoes")
                ),
                Playlist(
                    id = "playlist_rainy_lounge",
                    name = "Velvet Rain & Noir",
                    description = "Warm melancholic jazz and stormy nights",
                    gradientColors = listOf(SunsetGold, HotPink),
                    iconName = "cloud",
                    songIds = listOf("track_3_midnight_rain", "track_2_cosmic_lofi")
                )
            )
        }

        private fun getMoodStations(): List<MoodStation> {
            return listOf(
                MoodStation(
                    id = "mood_cosmic",
                    title = "Cosmic Chill",
                    emoji = "🌌",
                    description = "Weightless ambient & nebula soundscapes",
                    accentColor = PastelLavender,
                    songIds = listOf("track_4_solar_echoes", "track_2_cosmic_lofi")
                ),
                MoodStation(
                    id = "mood_neon",
                    title = "Neon Drive",
                    emoji = "⚡",
                    description = "High-octane synthwave & midnight velocity",
                    accentColor = NeonCyan,
                    songIds = listOf("track_1_neon_drift", "track_5_cyber_pulse")
                ),
                MoodStation(
                    id = "mood_lofi",
                    title = "Cozy Lo-Fi",
                    emoji = "☕",
                    description = "Warm vinyl crackle & warm Rhodes chords",
                    accentColor = SunsetGold,
                    songIds = listOf("track_2_cosmic_lofi", "track_3_midnight_rain")
                ),
                MoodStation(
                    id = "mood_rain",
                    title = "Rainy Street",
                    emoji = "🌧️",
                    description = "Mood-drenched jazz & nocturnal vibes",
                    accentColor = HotPink,
                    songIds = listOf("track_3_midnight_rain", "track_1_neon_drift")
                ),
                MoodStation(
                    id = "mood_zen",
                    title = "Zen Meditation",
                    emoji = "🧘",
                    description = "3D binaural relaxation & harmonic drones",
                    accentColor = AcidGreen,
                    songIds = listOf("track_4_solar_echoes")
                )
            )
        }
    }
}
