package com.example.audio

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.R
import com.example.data.model.LyricLine
import com.example.data.model.Song
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.CelestialBlue
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.SunsetGold
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

object AudioFileReader {

    private val colorPalettes = listOf(
        NeonCyan to ElectricPurple,
        PastelLavender to CyberViolet,
        HotPink to CelestialBlue,
        SunsetGold to CyberViolet,
        AcidGreen to ElectricPurple,
        CelestialBlue to HotPink
    )

    private val defaultArtworks = listOf(
        R.drawable.img_album_neon_drift,
        R.drawable.img_album_cosmic_lofi,
        R.drawable.img_album_midnight_rain,
        R.drawable.img_album_solar_echoes
    )

    /**
     * Reads metadata and embedded cover art from any content/file Uri using MediaMetadataRetriever
     */
    fun readSongFromUri(context: Context, uri: Uri): Song? {
        val retriever = MediaMetadataRetriever()
        return try {
            try {
                retriever.setDataSource(context, uri)
            } catch (e: Exception) {
                // If setDataSource with context fails, try opening file descriptor
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    retriever.setDataSource(pfd.fileDescriptor)
                } ?: return null
            }

            var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            var artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            var album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: "Audio Track"
            val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "audio/mpeg"
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)

            // Extract embedded album art (ID3 APIC frame)
            val embeddedPicture = retriever.embeddedPicture

            // Query filename and size from content resolver if title is missing
            var fileName: String? = null
            var fileSize = 0L
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) fileName = cursor.getString(nameIdx)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx)
                    }
                }
            } catch (_: Exception) {}

            if (title.isNullOrBlank()) {
                title = fileName?.substringBeforeLast(".") ?: uri.lastPathSegment?.substringBeforeLast(".") ?: "Audio Track"
            }
            if (artist.isNullOrBlank()) {
                artist = "Local Artist"
            }
            if (album.isNullOrBlank()) {
                album = "Imported Audio"
            }

            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val durationSec = (durationMs / 1000).toInt().coerceAtLeast(5)
            val bitrateKbps = (bitrateStr?.toIntOrNull() ?: 320000) / 1000

            val paletteIndex = kotlin.math.abs((title + artist).hashCode()) % colorPalettes.size
            val (primCol, secCol) = colorPalettes[paletteIndex]
            val defaultArt = defaultArtworks[kotlin.math.abs(title.hashCode()) % defaultArtworks.size]

            // Check if there are default or auto-generated lyrics
            val lyrics = listOf(
                LyricLine(0, "♪ $title by $artist ♪", "Lossless Audio Playback"),
                LyricLine(10000, "Bitrate: ${bitrateKbps}kbps • Frequency: 44.1kHz", "High Fidelity Stream"),
                LyricLine(25000, "Dolby Master Dynamic Equalizer Active", "Spatial Enhancer"),
                LyricLine(50000, "Enjoying lossless music with Bluemoon", "Studio Master")
            )

            Song(
                id = "imported_${System.currentTimeMillis()}_${(0..999).random()}",
                title = title,
                artist = artist,
                album = album,
                durationSeconds = durationSec,
                artworkResId = defaultArt,
                artworkBytes = embeddedPicture,
                artworkUri = null,
                primaryColor = primCol,
                secondaryColor = secCol,
                genre = genre,
                bpm = 120,
                synthScaleType = "CUSTOM_MEDIA",
                lyrics = lyrics,
                isFavorite = false,
                audioUri = uri.toString(),
                isLocal = true,
                bitRateKbps = bitrateKbps,
                sampleRateHz = 44100,
                filePath = fileName ?: uri.path,
                fileSize = fileSize,
                mimeType = mimeType,
                year = year,
                folderName = "Imported"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Scans MediaStore for all audio files stored on device
     */
    fun scanMediaStoreAudio(context: Context): List<Song> {
        val result = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val sizeCol = it.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val mimeCol = it.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val albumIdCol = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Track $id"
                    val artist = it.getString(artistCol) ?: "Unknown Artist"
                    val album = it.getString(albumCol) ?: "Unknown Album"
                    val durationMs = it.getLong(durCol)
                    val dataPath = if (dataCol != -1) it.getString(dataCol) else null
                    val fileSize = if (sizeCol != -1) it.getLong(sizeCol) else 0L
                    val mimeType = if (mimeCol != -1) it.getString(mimeCol) ?: "audio/mpeg" else "audio/mpeg"
                    val albumId = if (albumIdCol != -1) it.getLong(albumIdCol) else -1L

                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    // Album artwork Uri via album id
                    val albumArtUri = if (albumId != -1L) {
                        Uri.parse("content://media/external/audio/albumart/$albumId").toString()
                    } else null

                    val paletteIndex = kotlin.math.abs((title + artist).hashCode()) % colorPalettes.size
                    val (primCol, secCol) = colorPalettes[paletteIndex]
                    val defaultArt = defaultArtworks[kotlin.math.abs(title.hashCode()) % defaultArtworks.size]

                    val folder = dataPath?.let { p ->
                        val parts = p.split("/")
                        if (parts.size >= 2) parts[parts.size - 2] else "Device Storage"
                    } ?: "Device Storage"

                    val lyrics = listOf(
                        LyricLine(0, "♪ $title ♪", artist),
                        LyricLine(12000, "Folder: $folder", "Device Audio"),
                        LyricLine(30000, "Master Equalizer & 3D Spatial Audio Enabled", "Bluemoon Audio")
                    )

                    result.add(
                        Song(
                            id = "device_audio_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Local Media" else album,
                            durationSeconds = (durationMs / 1000).toInt().coerceAtLeast(5),
                            artworkResId = defaultArt,
                            artworkBytes = null,
                            artworkUri = albumArtUri,
                            primaryColor = primCol,
                            secondaryColor = secCol,
                            genre = "Local Storage",
                            bpm = 120,
                            synthScaleType = "CUSTOM_MEDIA",
                            lyrics = lyrics,
                            isFavorite = false,
                            audioUri = contentUri.toString(),
                            isLocal = true,
                            bitRateKbps = 320,
                            sampleRateHz = 44100,
                            filePath = dataPath ?: contentUri.path,
                            fileSize = fileSize,
                            mimeType = mimeType,
                            folderName = folder
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * Parses standard .LRC synchronized lyric files
     * Example format: [00:14.50]Line of lyric text
     */
    fun parseLrcContent(lrcText: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2,3}))?\\](.*)")

        lrcText.lines().forEach { rawLine ->
            val matcher = pattern.matcher(rawLine.trim())
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msStr = matcher.group(3) ?: "00"
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = matcher.group(4)?.trim().orEmpty()
                if (text.isNotBlank()) {
                    lines.add(LyricLine(timeMs = totalMs, text = text, vibeNote = "Synced Lyric"))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    /**
     * Reads LRC file from Uri
     */
    fun readLrcFromUri(contentResolver: ContentResolver, uri: Uri): List<LyricLine> {
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    val content = reader.readText()
                    parseLrcContent(content)
                }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
