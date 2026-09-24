package com.example.player

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.SongEntity
import com.example.data.TitonoxRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class SongItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val genre: String = "Cyber Electronic",
    val isFavorite: Boolean = false,
    val isBuiltIn: Boolean = false
)

class MusicManager(
    private val context: Context,
    private val repository: TitonoxRepository,
    private val scope: CoroutineScope
) {
    private var mediaPlayer: MediaPlayer? = null
    private var synthTrack: AudioTrack? = null
    private var progressJob: Job? = null
    private var synthJob: Job? = null

    private val builtInCyberSongs = listOf(
        SongItem(
            id = -101,
            title = "TITONOX Core Reactor",
            artist = "Aditya Yadav",
            album = "Cyber Pulse 2099",
            durationMs = 186000L,
            contentUri = "builtin://titonox_core",
            genre = "Cyber Ambient",
            isFavorite = true,
            isBuiltIn = true
        ),
        SongItem(
            id = -102,
            title = "Neon Velocity",
            artist = "TITONOX Synth",
            album = "Quantum Highway",
            durationMs = 214000L,
            contentUri = "builtin://neon_velocity",
            genre = "Synthwave",
            isBuiltIn = true
        ),
        SongItem(
            id = -103,
            title = "Digital Consciousness",
            artist = "Cyber Grid",
            album = "Neural Interface",
            durationMs = 195000L,
            contentUri = "builtin://digital_mind",
            genre = "Future Bass",
            isBuiltIn = true
        ),
        SongItem(
            id = -104,
            title = "Orbital Relay",
            artist = "TITONOX Audio",
            album = "Deep Space Matrix",
            durationMs = 240000L,
            contentUri = "builtin://orbital_relay",
            genre = "Space Chill",
            isBuiltIn = true
        )
    )

    private val _songs = MutableStateFlow<List<SongItem>>(builtInCyberSongs)
    val songs: StateFlow<List<SongItem>> = _songs.asStateFlow()

    private val _currentSong = MutableStateFlow<SongItem?>(builtInCyberSongs.first())
    val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    // Real-time audio waveform spectrum levels (16 frequency bars for HUD visualizer)
    private val _waveAmplitudes = MutableStateFlow<List<Float>>(List(16) { 0.15f })
    val waveAmplitudes: StateFlow<List<Float>> = _waveAmplitudes.asStateFlow()

    init {
        // Automatically attempt MediaStore scan on launch
        scanDeviceMusic()
        startVisualizerTicker()
    }

    fun hasAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun scanDeviceMusic() {
        scope.launch(Dispatchers.IO) {
            val list = mutableListOf<SongItem>()
            if (hasAudioPermission()) {
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION
                )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

                try {
                    context.contentResolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        "${MediaStore.Audio.Media.TITLE} ASC"
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idCol)
                            val title = cursor.getString(titleCol) ?: "Unknown Track"
                            val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                            val album = cursor.getString(albumCol) ?: "Unknown Album"
                            val duration = cursor.getLong(durCol)
                            val contentUri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                id
                            ).toString()

                            list.add(
                                SongItem(
                                    id = id,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    durationMs = if (duration > 0) duration else 180000L,
                                    contentUri = contentUri,
                                    genre = "Device Audio",
                                    isFavorite = false,
                                    isBuiltIn = false
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MusicManager", "MediaStore query error", e)
                }
            }

            // Combine scanned tracks with the high-tech TITONOX built-in themes
            val combined = if (list.isNotEmpty()) {
                list + builtInCyberSongs
            } else {
                builtInCyberSongs
            }

            _songs.value = combined
            if (_currentSong.value == null && combined.isNotEmpty()) {
                _currentSong.value = combined.first()
            }
        }
    }

    fun playSong(song: SongItem) {
        _currentSong.value = song
        _playbackPositionMs.value = 0L

        stopSynth()
        mediaPlayer?.release()
        mediaPlayer = null

        if (song.isBuiltIn) {
            // Play synthesized musical tones via AudioTrack
            playSynth(song)
        } else {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, Uri.parse(song.contentUri))
                    prepare()
                    start()
                    setOnCompletionListener {
                        onTrackFinished()
                    }
                }
                _isPlaying.value = true
            } catch (e: Exception) {
                Log.e("MusicManager", "Failed to play MediaStore track, falling back to synth", e)
                playSynth(song)
            }
        }

        startProgressTicker()
    }

    fun togglePlayPause() {
        val current = _currentSong.value ?: return
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        mediaPlayer?.pause()
        stopSynth()
        progressJob?.cancel()
    }

    fun resume() {
        val current = _currentSong.value ?: return
        _isPlaying.value = true
        if (current.isBuiltIn) {
            playSynth(current)
        } else {
            if (mediaPlayer == null) {
                playSong(current)
            } else {
                mediaPlayer?.start()
            }
        }
        startProgressTicker()
    }

    fun next() {
        val list = _songs.value
        if (list.isEmpty()) return
        val current = _currentSong.value
        val currentIndex = list.indexOfFirst { it.id == current?.id }

        val nextIndex = if (_isShuffle.value) {
            list.indices.random()
        } else {
            (currentIndex + 1) % list.size
        }

        playSong(list[nextIndex])
    }

    fun previous() {
        val list = _songs.value
        if (list.isEmpty()) return
        val current = _currentSong.value
        val currentIndex = list.indexOfFirst { it.id == current?.id }

        val prevIndex = if (currentIndex <= 0) list.size - 1 else currentIndex - 1
        playSong(list[prevIndex])
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    fun toggleFavorite(song: SongItem) {
        val updated = _songs.value.map {
            if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _songs.value = updated
        if (_currentSong.value?.id == song.id) {
            _currentSong.value = _currentSong.value?.copy(isFavorite = !song.isFavorite)
        }
    }

    private fun onTrackFinished() {
        if (_isRepeat.value) {
            _currentSong.value?.let { playSong(it) }
        } else {
            next()
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                delay(1000)
                val current = _currentSong.value ?: break
                if (mediaPlayer != null) {
                    try {
                        _playbackPositionMs.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    } catch (e: Exception) {
                        _playbackPositionMs.value += 1000
                    }
                } else {
                    _playbackPositionMs.value += 1000
                }

                if (_playbackPositionMs.value >= current.durationMs) {
                    onTrackFinished()
                }
            }
        }
    }

    private fun startVisualizerTicker() {
        scope.launch {
            var phase = 0f
            while (isActive) {
                delay(80)
                if (_isPlaying.value) {
                    phase += 0.25f
                    val newAmps = (0 until 16).map { idx ->
                        val base = ((sin(phase + idx * 0.45) + 1.0) / 2.0).toFloat()
                        val flutter = (Math.random() * 0.35).toFloat()
                        (base * 0.7f + flutter).coerceIn(0.12f, 1.0f)
                    }
                    _waveAmplitudes.value = newAmps
                } else {
                    // Idle subtle breathing wave
                    _waveAmplitudes.value = List(16) { 0.12f }
                }
            }
        }
    }

    // High-tech Cyber Synthesizer: produces harmonic soothing chords through AudioTrack
    private fun playSynth(song: SongItem) {
        stopSynth()
        _isPlaying.value = true

        synthJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                val track = AudioTrack.Builder()
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
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize.coerceAtLeast(4096))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                synthTrack = track
                track.play()

                val buffer = ShortArray(1024)
                var sampleIndex = 0L

                // Base frequencies for futuristic cyberpunk chill
                val notes = when (song.id) {
                    -101L -> listOf(110.0, 164.81, 220.0, 329.63) // A Minor
                    -102L -> listOf(130.81, 196.0, 261.63, 392.0) // C Major
                    -103L -> listOf(98.0, 146.83, 196.0, 293.66) // G Pentatonic
                    else -> listOf(123.47, 185.0, 246.94, 370.0) // B Minor
                }

                while (isActive && _isPlaying.value) {
                    for (i in buffer.indices) {
                        val t = (sampleIndex + i).toDouble() / sampleRate
                        val noteFreq = notes[((sampleIndex + i) / 8000).toInt() % notes.size]
                        // Harmonic synth wave
                        val wave = 0.45 * sin(2.0 * Math.PI * noteFreq * t) +
                                   0.25 * sin(4.0 * Math.PI * noteFreq * t) +
                                   0.15 * sin(2.0 * Math.PI * (noteFreq * 0.5) * t)

                        buffer[i] = (wave * 5500).toInt().toShort()
                    }
                    sampleIndex += buffer.size
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.w("MusicManager", "AudioTrack synth note", e)
            }
        }
    }

    private fun stopSynth() {
        synthJob?.cancel()
        synthJob = null
        try {
            synthTrack?.stop()
            synthTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        synthTrack = null
    }

    fun release() {
        stopSynth()
        mediaPlayer?.release()
        mediaPlayer = null
        progressJob?.cancel()
    }
}
