package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.AudioFileReader
import com.example.data.model.EqualizerState
import com.example.data.model.LyricLine
import com.example.data.model.MoodStation
import com.example.data.model.Playlist
import com.example.data.model.RepeatMode
import com.example.data.model.ReverbPreset
import com.example.data.model.Song
import com.example.data.model.VisualizerMode
import com.example.data.repository.SongRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SortOrder(val label: String) {
    TITLE("Title (A-Z)"),
    ARTIST("Artist"),
    DURATION("Duration"),
    FOLDER("Folder / Storage")
}

class MusicPlayerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: SongRepository = SongRepository()
    private val audioEngine = AudioEngine(application.applicationContext)

    val songs: StateFlow<List<Song>> = repository.songs
    val playlists: StateFlow<List<Playlist>> = repository.playlists
    val moods: StateFlow<List<MoodStation>> = repository.moods

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionMs: StateFlow<Long> = audioEngine.currentPositionMs
    val durationMs: StateFlow<Long> = audioEngine.durationMs
    val spectrumAmplitudes: StateFlow<List<Float>> = audioEngine.spectrumAmplitudes
    val waveformEnergy: StateFlow<Float> = audioEngine.waveformEnergy

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _visualizerMode = MutableStateFlow(VisualizerMode.VINYL)
    val visualizerMode: StateFlow<VisualizerMode> = _visualizerMode.asStateFlow()

    private val _equalizerState = MutableStateFlow(EqualizerState())
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()
    private var sleepTimerJob: Job? = null

    private val _selectedMoodId = MutableStateFlow<String?>(null)
    val selectedMoodId: StateFlow<String?> = _selectedMoodId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val currentLyricIndex: StateFlow<Int> = combine(_currentSong, currentPositionMs) { song: Song?, posMs: Long ->
        if (song == null || song.lyrics.isEmpty()) -1
        else {
            val idx = song.lyrics.indexOfLast { it.timeMs <= posMs }
            if (idx >= 0) idx else 0
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val filteredSongs: StateFlow<List<Song>> = combine(
        songs,
        _searchQuery,
        _selectedMoodId,
        _sortOrder,
        _selectedFolder
    ) { allSongs: List<Song>, query: String, moodId: String?, sort: SortOrder, folder: String? ->
        var list = allSongs
        if (moodId != null) {
            val mood = moods.value.find { it.id == moodId }
            if (mood != null) {
                list = list.filter { s -> mood.songIds.contains(s.id) }
            }
        }
        if (folder != null) {
            list = list.filter { s -> s.folderName == folder }
        }
        if (query.isNotBlank()) {
            list = list.filter { s ->
                s.title.contains(query, ignoreCase = true) ||
                s.artist.contains(query, ignoreCase = true) ||
                s.album.contains(query, ignoreCase = true) ||
                s.genre.contains(query, ignoreCase = true) ||
                (s.filePath?.contains(query, ignoreCase = true) == true)
            }
        }
        when (sort) {
            SortOrder.TITLE -> list.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> list.sortedBy { it.artist.lowercase() }
            SortOrder.DURATION -> list.sortedByDescending { it.durationSeconds }
            SortOrder.FOLDER -> list.sortedBy { it.folderName }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableFolders: StateFlow<List<String>> = songs.map { songList ->
        songList.map { it.folderName }.distinct()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        val initialList = repository.songs.value
        _queue.value = initialList
        if (initialList.isNotEmpty()) {
            _currentSong.value = initialList.first()
        }

        // Set engine track completion listener for seamless next song
        audioEngine.onCompletionListener = {
            viewModelScope.launch {
                onTrackFinished()
            }
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMoodFilter(moodId: String?) {
        _selectedMoodId.value = if (_selectedMoodId.value == moodId) null else moodId
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setSelectedFolder(folder: String?) {
        _selectedFolder.value = if (_selectedFolder.value == folder) null else folder
    }

    fun playTrack(song: Song, newQueue: List<Song>? = null) {
        if (newQueue != null) {
            _queue.value = newQueue
        }
        _currentSong.value = song
        audioEngine.playSong(song, 0L)
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            audioEngine.pause()
        } else {
            if (_currentSong.value == null && _queue.value.isNotEmpty()) {
                playTrack(_queue.value.first())
            } else {
                audioEngine.resume()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun nextTrack() {
        val q = _queue.value
        if (q.isEmpty()) return
        val current = _currentSong.value
        val currentIndex = q.indexOfFirst { it.id == current?.id }

        val nextIndex = when {
            _isShuffle.value -> {
                val available = (0 until q.size).filter { it != currentIndex }
                if (available.isNotEmpty()) available.random() else 0
            }
            currentIndex == -1 -> 0
            currentIndex < q.size - 1 -> currentIndex + 1
            _repeatMode.value == RepeatMode.ALL -> 0
            else -> currentIndex
        }

        val nextSong = q.getOrNull(nextIndex) ?: return
        playTrack(nextSong)
    }

    fun previousTrack() {
        val q = _queue.value
        if (q.isEmpty()) return
        val current = _currentSong.value
        val currentIndex = q.indexOfFirst { it.id == current?.id }

        if (currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = when {
            currentIndex > 0 -> currentIndex - 1
            _repeatMode.value == RepeatMode.ALL -> q.size - 1
            else -> 0
        }

        val prevSong = q.getOrNull(prevIndex) ?: return
        playTrack(prevSong)
    }

    private fun onTrackFinished() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentSong.value?.let { playTrack(it) }
            }
            RepeatMode.ALL -> {
                nextTrack()
            }
            RepeatMode.OFF -> {
                val q = _queue.value
                val current = _currentSong.value
                val currentIndex = q.indexOfFirst { it.id == current?.id }
                if (currentIndex in 0 until q.size - 1) {
                    nextTrack()
                } else {
                    audioEngine.stop()
                }
            }
        }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleFavorite(songId: String) {
        repository.toggleFavorite(songId)
        if (_currentSong.value?.id == songId) {
            _currentSong.update { it?.copy(isFavorite = !(it.isFavorite)) }
        }
    }

    fun setVisualizerMode(mode: VisualizerMode) {
        _visualizerMode.value = mode
    }

    fun updateEqualizerBand(bandIndex: Int, gainDb: Float) {
        val currentBands = _equalizerState.value.bands.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = gainDb.coerceIn(-10f, 10f)
            val updated = _equalizerState.value.copy(
                bands = currentBands,
                presetName = "Custom Preset"
            )
            _equalizerState.value = updated
            audioEngine.updateEqualizer(updated)
        }
    }

    fun setBassBoost(level: Float) {
        val updated = _equalizerState.value.copy(bassBoost = level.coerceIn(0f, 1f))
        _equalizerState.value = updated
        audioEngine.updateEqualizer(updated)
    }

    fun setSpatialAudio(level: Float) {
        val updated = _equalizerState.value.copy(spatialAudio = level.coerceIn(0f, 1f))
        _equalizerState.value = updated
        audioEngine.updateEqualizer(updated)
    }

    fun setReverbPreset(preset: ReverbPreset) {
        val updated = _equalizerState.value.copy(reverbPreset = preset)
        _equalizerState.value = updated
        audioEngine.updateEqualizer(updated)
    }

    fun setPlaybackSpeed(speed: Float) {
        val updated = _equalizerState.value.copy(
            playbackSpeed = speed,
            isNightcore = false,
            isDaycore = false
        )
        _equalizerState.value = updated
        audioEngine.updateEqualizer(updated)
    }

    fun toggleNightcore() {
        val enable = !_equalizerState.value.isNightcore
        val updated = _equalizerState.value.copy(
            isNightcore = enable,
            isDaycore = false,
            playbackSpeed = if (enable) 1.25f else 1.0f
        )
        _equalizerState.value = updated
        audioEngine.updateEqualizer(updated)
    }

    fun toggleDaycore() {
        val enable = !_equalizerState.value.isDaycore
        val updated = _equalizerState.value.copy(
            isDaycore = enable,
            isNightcore = false,
            playbackSpeed = if (enable) 0.8f else 1.0f,
            reverbPreset = if (enable) ReverbPreset.HALL else ReverbPreset.VINYL_LOUNGE
        )
        _equalizerState.value = updated
        audioEngine.updateEqualizer(updated)
    }

    fun applyEqPreset(preset: String) {
        val (bands, bassSpatial) = when (preset) {
            "Bass Boost" -> listOf(8f, 5f, 0f, 2f, 3f) to (0.9f to 0.5f)
            "Vocal Clarity" -> listOf(-2f, 1f, 6f, 5f, 2f) to (0.2f to 0.4f)
            "Lo-Fi Chill" -> listOf(4f, 2f, -3f, -5f, -8f) to (0.6f to 0.7f)
            "Neon Synth" -> listOf(6f, 3f, 1f, 6f, 8f) to (0.7f to 0.8f)
            "Acoustic" -> listOf(2f, 4f, 3f, 2f, 4f) to (0.3f to 0.5f)
            "Electronic" -> listOf(7f, 4f, -1f, 4f, 7f) to (0.8f to 0.6f)
            else -> listOf(0f, 0f, 0f, 0f, 0f) to (0.4f to 0.4f)
        }
        val updated = _equalizerState.value.copy(
            presetName = preset,
            bands = bands,
            bassBoost = bassSpatial.first,
            spatialAudio = bassSpatial.second
        )
        _equalizerState.value = updated
        audioEngine.updateEqualizer(updated)
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (remaining > 0 && isActive) {
                    delay(60_000L)
                    remaining--
                    _sleepTimerMinutes.value = remaining
                }
                audioEngine.pause()
                _sleepTimerMinutes.value = null
            }
        }
    }

    fun createPlaylist(name: String, description: String, colors: List<Color>) {
        repository.addCustomPlaylist(name, description, colors)
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        repository.addSongToPlaylist(playlistId, songId)
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        repository.removeSongFromPlaylist(playlistId, songId)
    }

    fun deletePlaylist(playlistId: String) {
        repository.deletePlaylist(playlistId)
    }

    fun importAudioFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isScanning.value = true
            val count = repository.importAudioUris(getApplication(), uris)
            _isScanning.value = false
            _statusMessage.value = "Imported $count audio track${if (count == 1) "" else "s"}"
            delay(3000)
            _statusMessage.value = null
        }
    }

    fun scanDeviceAudio() {
        viewModelScope.launch {
            _isScanning.value = true
            val count = repository.scanDeviceAudio(getApplication())
            _isScanning.value = false
            _statusMessage.value = if (count > 0) "Found $count tracks on device" else "No new tracks found in storage"
            delay(3000)
            _statusMessage.value = null
        }
    }

    fun importLrcFileForCurrentSong(uri: Uri) {
        val current = _currentSong.value ?: return
        viewModelScope.launch {
            val lyrics = AudioFileReader.readLrcFromUri(getApplication<Application>().contentResolver, uri)
            if (lyrics.isNotEmpty()) {
                repository.attachLyricsToSong(current.id, lyrics)
                _currentSong.update { it?.copy(lyrics = lyrics) }
                _statusMessage.value = "Loaded ${lyrics.size} synced lyric lines"
                delay(3000)
                _statusMessage.value = null
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
        sleepTimerJob?.cancel()
    }
}
