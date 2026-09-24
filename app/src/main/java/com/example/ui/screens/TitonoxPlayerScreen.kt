package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.SongItem
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.StateListening

enum class PlayerLibraryTab {
    ALL_SONGS,
    ARTISTS,
    ALBUMS,
    FAVORITES
}

@Composable
fun TitonoxPlayerScreen(
    viewModel: TitonoxViewModel,
    modifier: Modifier = Modifier
) {
    val music = viewModel.musicManager
    val songs by music.songs.collectAsState()
    val currentSong by music.currentSong.collectAsState()
    val isPlaying by music.isPlaying.collectAsState()
    val positionMs by music.playbackPositionMs.collectAsState()
    val isShuffle by music.isShuffle.collectAsState()
    val isRepeat by music.isRepeat.collectAsState()
    val waveAmps by music.waveAmplitudes.collectAsState()

    var selectedTab by remember { mutableStateOf(PlayerLibraryTab.ALL_SONGS) }

    // MediaStore Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            music.scanDeviceMusic()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "VinylDisc")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DiscRotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberBlack,
                        Color(0xFF070F2B),
                        CyberDarkNavy
                    )
                )
            )
            .testTag("titonox_player_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TITONOX PLAYER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = ElectricCyan
                        )
                    )
                    Text(
                        text = "NEON AUDIO ENGINE & MEDIA LIBRARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonBlue.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkNavy,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Scan", tint = ElectricCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SCAN MEDIA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HoloWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. NOW PLAYING HERO DECK
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotating Vinyl / Album Art Disc
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF030D1C))
                                .border(2.dp, if (isPlaying) ElectricCyan else NeonBlue, CircleShape)
                                .rotate(if (isPlaying) discRotation else 0f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan)
                            )
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = CyberBlack,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSong?.title ?: "Select a track",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = HoloWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "${currentSong?.artist ?: "TITONOX Studio"} • ${currentSong?.album ?: "Cyberpunk Ambient"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonBlue,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }

                        // Favorite heart button
                        IconButton(
                            onClick = { currentSong?.let { music.toggleFavorite(it) } }
                        ) {
                            Icon(
                                imageVector = if (currentSong?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (currentSong?.isFavorite == true) Color(0xFFFF5252) else HoloWhite.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 16-Band Dynamic Visualizer Spectrum Bars
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        val barWidth = 8.dp.toPx()
                        val spacing = (size.width - (16 * barWidth)) / 15f
                        for (i in 0 until 16) {
                            val amp = waveAmps.getOrElse(i) { 0.15f }
                            val barHeight = size.height * amp
                            val x = i * (barWidth + spacing)
                            val y = size.height - barHeight

                            drawLine(
                                brush = Brush.verticalGradient(
                                    colors = listOf(ElectricCyan, NeonBlue)
                                ),
                                start = Offset(x + barWidth / 2f, size.height),
                                end = Offset(x + barWidth / 2f, y),
                                strokeWidth = barWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Seek Slider
                    val duration = currentSong?.durationMs ?: 180000L
                    Slider(
                        value = positionMs.toFloat().coerceIn(0f, duration.toFloat()),
                        onValueChange = { music.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = HoloWhite.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Timers row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(positionMs),
                            style = MaterialTheme.typography.labelSmall.copy(color = HoloWhite.copy(alpha = 0.6f))
                        )
                        Text(
                            text = formatTime(duration),
                            style = MaterialTheme.typography.labelSmall.copy(color = HoloWhite.copy(alpha = 0.6f))
                        )
                    }

                    // Player Controls Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { music.toggleShuffle() }) {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle) ElectricCyan else HoloWhite.copy(alpha = 0.4f)
                            )
                        }

                        IconButton(onClick = { music.previous() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = HoloWhite, modifier = Modifier.size(28.dp))
                        }

                        // Big Play / Pause Button
                        IconButton(
                            onClick = { music.togglePlayPause() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = CyberBlack,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(onClick = { music.next() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = HoloWhite, modifier = Modifier.size(28.dp))
                        }

                        IconButton(onClick = { music.toggleRepeat() }) {
                            Icon(
                                if (isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (isRepeat) ElectricCyan else HoloWhite.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. LIBRARY TABS (All Songs, Artists, Albums, Favorites)
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = DarkNavy.copy(alpha = 0.85f),
                contentColor = ElectricCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = ElectricCyan
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                PlayerLibraryTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.name.replace('_', ' '),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) ElectricCyan else HoloWhite.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. LIBRARY TRACK LIST
            val displaySongs = when (selectedTab) {
                PlayerLibraryTab.ALL_SONGS -> songs
                PlayerLibraryTab.FAVORITES -> songs.filter { it.isFavorite }
                PlayerLibraryTab.ARTISTS -> songs.distinctBy { it.artist }
                PlayerLibraryTab.ALBUMS -> songs.distinctBy { it.album }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 60.dp)
            ) {
                if (displaySongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No songs in this view. Tap 'SCAN MEDIA' to load device audio.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = HoloWhite.copy(alpha = 0.5f))
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displaySongs) { song ->
                            val isCurrent = currentSong?.id == song.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { music.playSong(song) },
                                color = if (isCurrent) Color(0xFF031A3D) else DarkNavy.copy(alpha = 0.8f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) ElectricCyan else NeonBlue.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isCurrent) ElectricCyan else NeonBlue.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = if (isCurrent) CyberBlack else HoloWhite,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = if (isCurrent) ElectricCyan else HoloWhite,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${song.artist} • ${song.genre}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = HoloWhite.copy(alpha = 0.5f),
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Text(
                                        text = formatTime(song.durationMs),
                                        style = MaterialTheme.typography.labelSmall.copy(color = HoloWhite.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
