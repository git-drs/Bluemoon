package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CelestialBlue
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.SunsetGold

data class LyricLine(
    val timeMs: Long,
    val text: String,
    val vibeNote: String = ""
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val artworkResId: Int = com.example.R.drawable.img_album_neon_drift,
    val artworkBytes: ByteArray? = null,
    val artworkUri: String? = null,
    val primaryColor: Color = NeonCyan,
    val secondaryColor: Color = ElectricPurple,
    val genre: String = "Electronic",
    val bpm: Int = 110,
    val synthScaleType: String = "NEON_DRIVE",
    val lyrics: List<LyricLine> = emptyList(),
    val isFavorite: Boolean = false,
    val audioUri: String? = null,
    val isLocal: Boolean = false,
    val bitRateKbps: Int = 320,
    val sampleRateHz: Int = 44100,
    val filePath: String? = null,
    val fileSize: Long = 0L,
    val mimeType: String = "audio/mpeg",
    val year: String? = null,
    val folderName: String = "Library"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Song
        if (id != other.id) return false
        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (isFavorite != other.isFavorite) return false
        if (audioUri != other.audioUri) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + isFavorite.hashCode()
        return result
    }

    fun formattedFileSize(): String {
        if (fileSize <= 0) return "Embedded"
        val mb = fileSize / (1024.0 * 1024.0)
        return if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%d KB", fileSize / 1024)
    }

    fun formattedAudioFormat(): String {
        return when {
            mimeType.contains("flac", ignoreCase = true) || (filePath?.endsWith(".flac", ignoreCase = true) == true) -> "FLAC Lossless"
            mimeType.contains("wav", ignoreCase = true) || (filePath?.endsWith(".wav", ignoreCase = true) == true) -> "WAV PCM"
            mimeType.contains("aac", ignoreCase = true) || (filePath?.endsWith(".aac", ignoreCase = true) == true) -> "AAC HD"
            mimeType.contains("m4a", ignoreCase = true) || (filePath?.endsWith(".m4a", ignoreCase = true) == true) -> "M4A / ALAC"
            mimeType.contains("ogg", ignoreCase = true) || (filePath?.endsWith(".ogg", ignoreCase = true) == true) -> "OGG Vorbis"
            isLocal -> "MP3 Audio"
            else -> "32-bit Synth Stream"
        }
    }
}

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val gradientColors: List<Color> = listOf(CyberViolet, HotPink),
    val iconName: String = "playlist",
    val songIds: List<String> = emptyList(),
    val isCustom: Boolean = false
)

data class MoodStation(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val accentColor: Color,
    val songIds: List<String>
)

enum class VisualizerMode(val title: String, val icon: String) {
    VINYL("Vinyl Disc", "disc"),
    WAVEFORM("Waveform", "wave"),
    SPECTRUM("Frequency Bars", "bars"),
    LIQUID_ORB("Liquid Aura", "orb"),
    AURORA("Cyber Aurora", "aurora"),
    LYRICS("Lyrics", "lyrics")
}

enum class ReverbPreset(val displayName: String) {
    OFF("Studio Direct"),
    ROOM("Intimate Room"),
    HALL("Cosmic Hall"),
    CATHEDRAL("Cyber Cathedral"),
    VINYL_LOUNGE("Lo-Fi Lounge")
}

data class EqualizerState(
    val isEnabled: Boolean = true,
    val presetName: String = "Balanced Vibe",
    val bands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f), // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz (range -10 to +10 dB)
    val bassBoost: Float = 0.5f, // 0.0 to 1.0
    val spatialAudio: Float = 0.4f, // 0.0 to 1.0
    val reverbPreset: ReverbPreset = ReverbPreset.VINYL_LOUNGE,
    val playbackSpeed: Float = 1.0f,
    val isNightcore: Boolean = false,
    val isDaycore: Boolean = false
)

enum class RepeatMode {
    OFF, ALL, ONE
}
