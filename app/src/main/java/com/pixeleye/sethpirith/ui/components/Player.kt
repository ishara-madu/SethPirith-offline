package com.pixeleye.sethpirith.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pixeleye.sethpirith.R
import com.pixeleye.sethpirith.data.PirithDataProvider
import com.pixeleye.sethpirith.service.MediaPlayerService
import com.pixeleye.sethpirith.ui.theme.Primary
import com.pixeleye.sethpirith.ui.theme.Secondary
import com.pixeleye.sethpirith.ui.theme.Ternary
import com.pixeleye.sethpirith.ui.util.LimitCharacters
import com.pixeleye.sethpirith.ui.util.PirithPrefs
import com.pixeleye.sethpirith.ui.util.PirithPrefs.isOnceOpened
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun Player() {
    val context = LocalContext.current
    var mediaPlayerService by remember { mutableStateOf<MediaPlayerService?>(null) }
    var isPlaying by remember { mutableStateOf(PirithPrefs.isPlaying(context)) }
    var progress by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableIntStateOf(0) }
    var currentPosMs by remember { mutableIntStateOf(0) }
    var repeatOpacity by remember { mutableFloatStateOf(if (PirithPrefs.isRepeatEnabled(context)) 1.0f else 0.5f) }
    var shuffleOpacity by remember { mutableFloatStateOf(if (PirithPrefs.isShuffleEnabled(context)) 1.0f else 0.5f) }
    var isRepeatEnabled by remember { mutableStateOf(PirithPrefs.isRepeatEnabled(context)) }
    var isShuffleEnabled by remember { mutableStateOf(PirithPrefs.isShuffleEnabled(context)) }
    var playingID by remember {
        mutableIntStateOf(
            PirithPrefs.getLastAudioId(context).coerceAtLeast(0)
        )
    }
    val shouldAutoPlay by PirithPrefs.isPlayingFlow(context)
        .collectAsState(initial = PirithPrefs.getLastAudioId(context))


    val pirithList = PirithDataProvider.pirithList

    var playingTitle = stringResource(id = pirithList[playingID].titleResId)
    // Service connection
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // Cast the service binder and get the MediaPlayerService instance
                val binder = service as MediaPlayerService.MediaPlayerBinder
                mediaPlayerService = binder.getService()
                if (shouldAutoPlay as Boolean) {
                    if (mediaPlayerService?.isPlaying() != true || mediaPlayerService?.getPlayingId() != playingID) {
                        mediaPlayerService?.playTrack(playingID)
                    }
                } else {
                    if (!isOnceOpened) {
                        mediaPlayerService?.showTrack(playingID)
                        isOnceOpened = true
                    }
                }

            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mediaPlayerService = null
                android.util.Log.d("Player", "Service disconnected")
            }
        }
    }

    // Bind to service and start it
    LaunchedEffect(Unit) {
        val intent = Intent(context, MediaPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // Update UI with service state
    LaunchedEffect(mediaPlayerService) {
        while (isActive && mediaPlayerService != null) {
            mediaPlayerService?.let { service ->
                // Only update UI if service is in a valid state
                isPlaying = service.isPlaying()

                currentPosMs = service.getCurrentPosition()
                durationMs = service.getDuration()
                progress = if (durationMs > 0) currentPosMs.toFloat() / durationMs else 0f
                playingID = service.getPlayingId()
                playingTitle = service.getPlayingTitle()

            }
            delay(500)
        }
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    // Format milliseconds to mm:ss
    fun formatMs(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStartPercent = 8, topEndPercent = 8))
                .background(Secondary)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LimitCharacters(playingTitle, 25),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = progress,
                    onValueChange = { newValue ->
                        progress = newValue
                        val seekPos = (durationMs * newValue).toInt()
                        mediaPlayerService?.seekTo(seekPos)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Ternary,
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = formatMs(currentPosMs), color = Color.White, fontSize = 12.sp)
                    Text(text = formatMs(durationMs), color = Color.White, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 8.dp, 10.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                mediaPlayerService?.toggleShuffle()
                                isShuffleEnabled = !isShuffleEnabled
                                shuffleOpacity = if (isShuffleEnabled) 1.0f else 0.5f
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_shuffle_24),
                            contentDescription = "Shuffle",
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .alpha(shuffleOpacity)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White, RoundedCornerShape(100.dp))
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true) // Ripple effect spans the entire Box
                            ) { mediaPlayerService?.playPreviousTrack() }
                            .padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_skip_previous_24),
                            contentDescription = "Skip Previous",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Primary)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true) // Ripple effect spans the entire Box
                            ) {
                                mediaPlayerService?.togglePlayPause()
                            }
                            .padding(15.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
                            ),
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White, RoundedCornerShape(100.dp))
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true) // Ripple effect spans the entire Box
                            ) { mediaPlayerService?.playNextTrack() }
                            .padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_skip_next_24),
                            contentDescription = "Skip Next",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clickable {
                                mediaPlayerService?.toggleRepeat()
                                isRepeatEnabled = !isRepeatEnabled
                                repeatOpacity = if (isRepeatEnabled) 1.0f else 0.5f
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_repeat_24),
                            contentDescription = "Repeat",
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .alpha(repeatOpacity)
                        )
                    }
                }
            }
        }
    }
}