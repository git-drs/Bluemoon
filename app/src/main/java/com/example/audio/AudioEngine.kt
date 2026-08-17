package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import com.example.data.model.EqualizerState
import com.example.data.model.ReverbPreset
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class AudioEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var synthJob: Job? = null
    private var trackerJob: Job? = null

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null

    // Android AudioFx hardware effects
    private var hardwareEqualizer: Equalizer? = null
    private var hardwareBassBoost: BassBoost? = null
    private var hardwareVirtualizer: Virtualizer? = null
    private var hardwareReverb: PresetReverb? = null
    private var systemVisualizer: Visualizer? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180_000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    // Real-time audio spectrum visualization amplitudes (16 bands)
    private val _spectrumAmplitudes = MutableStateFlow<List<Float>>(List(16) { 0.08f })
    val spectrumAmplitudes: StateFlow<List<Float>> = _spectrumAmplitudes.asStateFlow()

    // Real-time waveform amplitude (for vinyl pulse and liquid orb)
    private val _waveformEnergy = MutableStateFlow(0.15f)
    val waveformEnergy: StateFlow<Float> = _waveformEnergy.asStateFlow()

    private var currentSong: Song? = null
    private var eqState = EqualizerState()
    var onCompletionListener: (() -> Unit)? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    fun updateEqualizer(newState: EqualizerState) {
        eqState = newState

        // Apply to hardware effects if MediaPlayer is active
        applyHardwareEffects()

        // Apply playback speed / pitch if MediaPlayer is active
        mediaPlayer?.let { player ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val targetSpeed = when {
                        newState.isNightcore -> 1.25f
                        newState.isDaycore -> 0.8f
                        else -> newState.playbackSpeed
                    }
                    val targetPitch = when {
                        newState.isNightcore -> 1.25f
                        newState.isDaycore -> 0.8f
                        else -> 1.0f
                    }
                    player.playbackParams = player.playbackParams.setSpeed(targetSpeed).setPitch(targetPitch)
                }
            } catch (_: Exception) {}
        }
    }

    private fun attachHardwareEffects(sessionId: Int) {
        releaseHardwareEffects()
        try {
            // Equalizer
            hardwareEqualizer = Equalizer(0, sessionId).apply {
                enabled = eqState.isEnabled
            }

            // Bass Boost
            hardwareBassBoost = BassBoost(0, sessionId).apply {
                enabled = eqState.isEnabled
                setStrength((eqState.bassBoost * 1000).toInt().coerceIn(0, 1000).toShort())
            }

            // Virtualizer / Spatial Audio
            hardwareVirtualizer = Virtualizer(0, sessionId).apply {
                enabled = eqState.isEnabled
                setStrength((eqState.spatialAudio * 1000).toInt().coerceIn(0, 1000).toShort())
            }

            // Preset Reverb
            hardwareReverb = PresetReverb(0, sessionId).apply {
                preset = when (eqState.reverbPreset) {
                    ReverbPreset.OFF -> PresetReverb.PRESET_NONE
                    ReverbPreset.ROOM -> PresetReverb.PRESET_SMALLROOM
                    ReverbPreset.HALL -> PresetReverb.PRESET_LARGEHALL
                    ReverbPreset.CATHEDRAL -> PresetReverb.PRESET_LARGEROOM
                    ReverbPreset.VINYL_LOUNGE -> PresetReverb.PRESET_MEDIUMHALL
                }
                enabled = eqState.isEnabled && eqState.reverbPreset != ReverbPreset.OFF
            }

            // System Visualizer for real FFT data
            try {
                systemVisualizer = Visualizer(sessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                            waveform?.let { bytes ->
                                var sum = 0f
                                for (b in bytes) {
                                    sum += abs((b.toInt() - 128) / 128f)
                                }
                                val energy = (sum / bytes.size).coerceIn(0.05f, 1.0f)
                                _waveformEnergy.value = energy
                            }
                        }

                        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                            fft?.let { bytes ->
                                val n = bytes.size / 2
                                val bands = MutableList(16) { 0.05f }
                                val step = (n / 16).coerceAtLeast(1)
                                for (i in 0 until 16) {
                                    var magSum = 0f
                                    val start = i * step
                                    val end = ((i + 1) * step).coerceAtMost(n)
                                    for (k in start until end) {
                                        val real = bytes[k * 2].toFloat()
                                        val imag = bytes[k * 2 + 1].toFloat()
                                        magSum += sqrt(real * real + imag * imag)
                                    }
                                    val avgMag = (magSum / (end - start).coerceAtLeast(1) / 128f).coerceIn(0.05f, 1.0f)
                                    bands[i] = avgMag
                                }
                                _spectrumAmplitudes.value = bands
                            }
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, true, true)
                    enabled = true
                }
            } catch (_: Exception) {}

            applyHardwareEffects()
        } catch (_: Exception) {}
    }

    private fun applyHardwareEffects() {
        try {
            hardwareEqualizer?.let { eq ->
                eq.enabled = eqState.isEnabled
                val minLevel = eq.bandLevelRange[0]
                val maxLevel = eq.bandLevelRange[1]
                val numBands = eq.numberOfBands.toInt()
                for (i in 0 until numBands.coerceAtMost(eqState.bands.size)) {
                    val gainDb = eqState.bands[i] // -10 to +10 dB
                    // map -10..+10 to minLevel..maxLevel (typically -1500 to +1500 mB)
                    val level = ((gainDb / 10f) * maxLevel).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                    eq.setBandLevel(i.toShort(), level)
                }
            }
            hardwareBassBoost?.let { bb ->
                bb.enabled = eqState.isEnabled
                bb.setStrength((eqState.bassBoost * 1000).toInt().coerceIn(0, 1000).toShort())
            }
            hardwareVirtualizer?.let { virt ->
                virt.enabled = eqState.isEnabled
                virt.setStrength((eqState.spatialAudio * 1000).toInt().coerceIn(0, 1000).toShort())
            }
            hardwareReverb?.let { rev ->
                rev.preset = when (eqState.reverbPreset) {
                    ReverbPreset.OFF -> PresetReverb.PRESET_NONE
                    ReverbPreset.ROOM -> PresetReverb.PRESET_SMALLROOM
                    ReverbPreset.HALL -> PresetReverb.PRESET_LARGEHALL
                    ReverbPreset.CATHEDRAL -> PresetReverb.PRESET_LARGEROOM
                    ReverbPreset.VINYL_LOUNGE -> PresetReverb.PRESET_MEDIUMHALL
                }
                rev.enabled = eqState.isEnabled && eqState.reverbPreset != ReverbPreset.OFF
            }
        } catch (_: Exception) {}
    }

    private fun releaseHardwareEffects() {
        try {
            systemVisualizer?.enabled = false
            systemVisualizer?.release()
        } catch (_: Exception) {}
        systemVisualizer = null

        try {
            hardwareEqualizer?.release()
        } catch (_: Exception) {}
        hardwareEqualizer = null

        try {
            hardwareBassBoost?.release()
        } catch (_: Exception) {}
        hardwareBassBoost = null

        try {
            hardwareVirtualizer?.release()
        } catch (_: Exception) {}
        hardwareVirtualizer = null

        try {
            hardwareReverb?.release()
        } catch (_: Exception) {}
        hardwareReverb = null
    }

    fun playSong(song: Song, startPositionMs: Long = 0L) {
        stop()
        currentSong = song
        _durationMs.value = song.durationSeconds * 1000L
        _currentPositionMs.value = startPositionMs

        requestAudioFocus()

        if (song.audioUri != null) {
            playMediaUri(song.audioUri, startPositionMs)
        } else {
            playProceduralSynth(song, startPositionMs)
        }
        _isPlaying.value = true
    }

    fun pause() {
        _isPlaying.value = false
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        synthJob?.cancel()
    }

    fun resume() {
        val song = currentSong ?: return
        requestAudioFocus()
        if (song.audioUri != null && mediaPlayer != null) {
            try {
                mediaPlayer?.start()
                _isPlaying.value = true
                startMediaPositionTracker()
            } catch (e: Exception) {
                playMediaUri(song.audioUri, _currentPositionMs.value)
            }
        } else if (song.audioUri != null) {
            playMediaUri(song.audioUri, _currentPositionMs.value)
        } else {
            playProceduralSynth(song, _currentPositionMs.value)
            _isPlaying.value = true
        }
    }

    fun seekTo(positionMs: Long) {
        val validPos = positionMs.coerceIn(0L, _durationMs.value)
        _currentPositionMs.value = validPos
        mediaPlayer?.let {
            try {
                it.seekTo(validPos.toInt())
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        _isPlaying.value = false
        trackerJob?.cancel()
        trackerJob = null
        synthJob?.cancel()
        synthJob = null

        releaseHardwareEffects()

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null

        _spectrumAmplitudes.value = List(16) { 0.05f }
        _waveformEnergy.value = 0.05f
    }

    private fun playMediaUri(uriString: String, startPositionMs: Long) {
        try {
            val uri = Uri.parse(uriString)
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                    if (startPositionMs > 0) {
                        mp.seekTo(startPositionMs.toInt())
                    }
                    attachHardwareEffects(mp.audioSessionId)
                    mp.start()
                    _isPlaying.value = true
                    startMediaPositionTracker()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    onCompletionListener?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback to synth if native file playback runs into decoder error
                    currentSong?.let { playProceduralSynth(it, _currentPositionMs.value) }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to synth if local media decoding fails
            currentSong?.let { playProceduralSynth(it, startPositionMs) }
        }
    }

    private fun startMediaPositionTracker() {
        trackerJob?.cancel()
        trackerJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            val cur = player.currentPosition.toLong()
                            _currentPositionMs.value = cur
                            if (player.duration > 0) {
                                _durationMs.value = player.duration.toLong()
                            }

                            // If hardware Visualizer is not active/available, generate responsive dynamic spectrum
                            if (systemVisualizer == null || !systemVisualizer!!.enabled) {
                                val progress = cur / 1000f
                                val bassBoostGain = 1.0f + eqState.bassBoost * 0.8f + (eqState.bands.getOrElse(0) { 0f } / 15f)
                                val trebleGain = 1.0f + (eqState.bands.getOrElse(4) { 0f } / 15f)
                                val beat = (sin(progress * 3.8 * PI).toFloat() * 0.5f + 0.5f).coerceIn(0f, 1f)

                                val bands = List(16) { i ->
                                    val waveMod = (sin(progress * (2f + i * 0.35f) + i).toFloat() * 0.5f + 0.5f)
                                    val bassComp = if (i < 4) (0.35f * beat * bassBoostGain) else 0.1f
                                    val trebleComp = if (i > 11) (0.3f * waveMod * trebleGain) else 0f
                                    (0.12f + 0.45f * waveMod + bassComp + trebleComp).coerceIn(0.08f, 0.98f)
                                }
                                _spectrumAmplitudes.value = bands
                                _waveformEnergy.value = (0.2f + 0.65f * beat).coerceIn(0.1f, 1.0f)
                            }
                        }
                    } catch (_: Exception) {}
                }
                delay(40)
            }
        }
    }

    private fun playProceduralSynth(song: Song, startPositionMs: Long) {
        synthJob?.cancel()

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (_: Exception) {}

        synthJob = scope.launch {
            var currentSampleIndex = (startPositionMs * sampleRate / 1000L)
            val audioBuffer = ShortArray(bufferSize / 2) // 16-bit PCM stereo
            val random = Random(42)

            val (baseFreqs, scaleRatios) = when (song.synthScaleType) {
                "SYNTHWAVE_MAJOR" -> listOf(110.0, 130.81, 146.83, 164.81) to listOf(1.0, 1.25, 1.5, 1.875, 2.0)
                "LOFI_PENTATONIC" -> listOf(87.31, 98.0, 116.54, 130.81) to listOf(1.0, 1.2, 1.333, 1.5, 1.777)
                "JAZZ_CHILL_DORIAN" -> listOf(92.5, 110.0, 123.47, 138.59) to listOf(1.0, 1.189, 1.335, 1.587, 1.782)
                "SOLAR_AMBIENT" -> listOf(65.41, 77.78, 87.31, 98.0) to listOf(1.0, 1.5, 2.0, 2.5, 3.0)
                "CYBERPUNK_DRIVE" -> listOf(55.0, 61.74, 73.42, 82.41) to listOf(1.0, 1.122, 1.26, 1.498, 1.682)
                else -> listOf(110.0, 130.81, 146.83, 164.81) to listOf(1.0, 1.25, 1.5, 1.75, 2.0)
            }

            val bpm = song.bpm.toDouble()
            val beatPeriodSamples = (sampleRate * 60.0 / bpm).toLong()

            val delaySamples = (sampleRate * 0.25).toInt()
            val delayBufferL = FloatArray(delaySamples)
            val delayBufferR = FloatArray(delaySamples)
            var delayIdx = 0

            while (isActive && _isPlaying.value) {
                val totalDurationSamples = song.durationSeconds * sampleRate.toLong()
                if (currentSampleIndex >= totalDurationSamples) {
                    _currentPositionMs.value = _durationMs.value
                    _isPlaying.value = false
                    onCompletionListener?.invoke()
                    break
                }

                val speedFactor = when {
                    eqState.isNightcore -> 1.25f
                    eqState.isDaycore -> 0.8f
                    else -> eqState.playbackSpeed
                }

                val pitchFactor = when {
                    eqState.isNightcore -> 1.25f
                    eqState.isDaycore -> 0.8f
                    else -> 1.0f
                }

                val bassBoostGain = 1.0f + eqState.bassBoost * 0.8f + (eqState.bands.getOrElse(0) { 0f } / 15f)
                val trebleGain = 1.0f + (eqState.bands.getOrElse(4) { 0f } / 15f)
                val reverbAmount = when (eqState.reverbPreset) {
                    ReverbPreset.OFF -> 0.05f
                    ReverbPreset.ROOM -> 0.25f
                    ReverbPreset.HALL -> 0.55f
                    ReverbPreset.CATHEDRAL -> 0.75f
                    ReverbPreset.VINYL_LOUNGE -> 0.4f
                }

                var energyAccum = 0.0f

                for (i in 0 until audioBuffer.size step 2) {
                    val t = currentSampleIndex.toDouble() / sampleRate
                    val beatTime = (currentSampleIndex % beatPeriodSamples).toDouble() / beatPeriodSamples
                    val chordIdx = ((currentSampleIndex / (beatPeriodSamples * 4)) % baseFreqs.size).toInt()
                    val currentBaseFreq = baseFreqs[chordIdx] * pitchFactor

                    // Bassline
                    val bassEnv = (1.0 - beatTime * 0.7).coerceIn(0.0, 1.0)
                    val bassWave = sin(2.0 * PI * currentBaseFreq * t) + 0.3 * sin(4.0 * PI * currentBaseFreq * t)
                    val bassOut = bassWave * bassEnv * 0.22 * bassBoostGain

                    // Chords (3 voices)
                    var chordOut = 0.0
                    for (step in 0 until 3) {
                        val ratio = scaleRatios[(chordIdx + step * 2) % scaleRatios.size]
                        val chordFreq = currentBaseFreq * 2.0 * ratio
                        val padLfo = 0.8 + 0.2 * sin(2.0 * PI * 0.3 * t + step)
                        chordOut += sin(2.0 * PI * chordFreq * t) * 0.08 * padLfo
                    }

                    // Arpeggio
                    val arpStep = ((currentSampleIndex / (beatPeriodSamples / 4)) % scaleRatios.size).toInt()
                    val arpFreq = currentBaseFreq * 4.0 * scaleRatios[arpStep]
                    val arpEnv = (1.0 - (beatTime * 4.0 % 1.0) * 0.85).coerceIn(0.0, 1.0)
                    val arpOvertone = sin(2.0 * PI * arpFreq * t) + 0.25 * sin(4.0 * PI * arpFreq * t)
                    val arpOut = arpOvertone * arpEnv * 0.12 * trebleGain

                    // Lo-Fi vinyl crackle & beat
                    val crackle = if (random.nextFloat() > 0.985f) (random.nextFloat() * 2f - 1f) * 0.04 else 0.0
                    val kick = if (beatTime < 0.12) sin(2.0 * PI * (120.0 - beatTime * 600.0).coerceAtLeast(35.0) * t) * (1.0 - beatTime * 8.0).coerceAtLeast(0.0) * 0.3 * bassBoostGain else 0.0
                    val hihat = if (beatTime in 0.48..0.55 || beatTime in 0.95..0.99) (random.nextDouble() * 2.0 - 1.0) * 0.03 * trebleGain else 0.0

                    var rawLeft = (bassOut + chordOut + arpOut * 0.8 + kick + hihat + crackle).toFloat()
                    var rawRight = (bassOut + chordOut + arpOut * 1.2 + kick + hihat * 0.7 + crackle).toFloat()

                    // Spatial reverb delay
                    val delayedL = delayBufferL[delayIdx]
                    val delayedR = delayBufferR[delayIdx]
                    delayBufferL[delayIdx] = (rawLeft + delayedR * 0.45f * reverbAmount).coerceIn(-1f, 1f)
                    delayBufferR[delayIdx] = (rawRight + delayedL * 0.45f * reverbAmount).coerceIn(-1f, 1f)
                    delayIdx = (delayIdx + 1) % delaySamples

                    rawLeft += delayedL * reverbAmount
                    rawRight += delayedR * reverbAmount

                    val outL = (rawLeft.coerceIn(-0.95f, 0.95f) * 32767f).toInt().toShort()
                    val outR = (rawRight.coerceIn(-0.95f, 0.95f) * 32767f).toInt().toShort()

                    audioBuffer[i] = outL
                    audioBuffer[i + 1] = outR

                    energyAccum += abs(rawLeft)
                    currentSampleIndex = (currentSampleIndex + (1 * speedFactor).toLong().coerceAtLeast(1))
                }

                audioTrack?.write(audioBuffer, 0, audioBuffer.size)

                val currentSec = (currentSampleIndex / sampleRate).coerceAtMost(song.durationSeconds.toLong())
                _currentPositionMs.value = currentSec * 1000L

                val avgEnergy = (energyAccum / (audioBuffer.size / 2)).coerceIn(0.05f, 1.0f)
                _waveformEnergy.value = avgEnergy

                val beatFrac = ((currentSampleIndex % beatPeriodSamples).toFloat() / beatPeriodSamples)
                val beatPunch = if (beatFrac < 0.2f) (1f - beatFrac * 4f) else 0f
                val newSpectrum = List(16) { bandIdx ->
                    val waveMod = (sin(currentSec * (1.5f + bandIdx * 0.3f) + bandIdx).toFloat() * 0.5f + 0.5f)
                    val bassWeight = if (bandIdx < 4) (0.4f * avgEnergy * bassBoostGain + 0.5f * beatPunch) else (0.25f * avgEnergy)
                    val trebleWeight = if (bandIdx > 11) (0.35f * waveMod * trebleGain) else 0f
                    (bassWeight + trebleWeight + 0.3f * waveMod).coerceIn(0.08f, 0.98f)
                }
                _spectrumAmplitudes.value = newSpectrum
            }
        }
    }
}
