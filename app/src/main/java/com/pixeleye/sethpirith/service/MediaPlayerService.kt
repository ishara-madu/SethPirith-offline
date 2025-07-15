package com.pixeleye.sethpirith.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pixeleye.sethpirith.MainActivity
import com.pixeleye.sethpirith.R
import com.pixeleye.sethpirith.data.PirithDataProvider
import com.pixeleye.sethpirith.ui.util.PirithPrefs
import kotlinx.coroutines.*
import java.util.Locale
import kotlin.random.Random

class MediaPlayerService : Service() {
    companion object {
        const val ACTION_START_TIMER = "com.pixeleye.sethpirith.ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "com.pixeleye.sethpirith.ACTION_STOP_TIMER"
        const val ACTION_TIMER_UPDATE = "com.pixeleye.sethpirith.ACTION_TIMER_UPDATE"
        const val ACTION_TIMER_FINISHED = "com.pixeleye.sethpirith.ACTION_TIMER_FINISHED"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_REMAINING_TIME = "remaining_time"
    }

    private val binder = MediaPlayerBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var playingID: Int = 0
    private var isPlaying: Boolean = false
    private var isRepeatEnabled: Boolean = false
    private var isShuffleEnabled: Boolean = false
    private var playingTitle: String = ""
    private val pirithList = PirithDataProvider.pirithList
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null
    private var timerStartTime: Long = 0L
    private var timerDuration: Long = 0L
    private lateinit var localizedContext: Context

    private val localeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val localeCode = intent?.getStringExtra("localeCode") ?: PirithPrefs.getLocale(this@MediaPlayerService)
            localizedContext = createLocalizedContext(localeCode)
            if (playingID >= 0 && playingID < pirithList.size) {
                playingTitle = localizedContext.getString(PirithDataProvider.pirithList[playingID].titleResId)
            }
            updateNotification()
            Log.d("changedLocale", "Locale changed to: $localeCode")
        }
    }

    inner class MediaPlayerBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        localizedContext = createLocalizedContext(PirithPrefs.getLocale(this))
        ContextCompat.registerReceiver(
            this,
            localeChangeReceiver,
            IntentFilter("com.pixeleye.sethpirith.LOCALE_CHANGED"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        createNotificationChannel()
        playingID = PirithPrefs.getLastAudioId(this).coerceAtLeast(0)
        isRepeatEnabled = PirithPrefs.isRepeatEnabled(this)
        isShuffleEnabled = PirithPrefs.isShuffleEnabled(this)
        PirithPrefs.getTimerState(this)?.let { (startTime, duration) ->
            val timeElapsed = System.currentTimeMillis() - startTime
            val timeLeft = duration - timeElapsed
            if (timeLeft > 0) {
                startTimer(timeLeft, startTime)
            } else {
                PirithPrefs.clearTimerState(this)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY_PAUSE" -> togglePlayPause()
            "NEXT" -> playNextTrack()
            "PREVIOUS" -> playPreviousTrack()
            "CLOSE" -> stop()
            ACTION_START_TIMER -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                if (duration > 0) {
                    startTimer(duration)
                }
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media_player_channel",
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification() {
        Log.d("changedLocale", "updateNotification: Locale=${localizedContext.resources.configuration.locale.language}")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, MediaPlayerService::class.java).apply { action = "PLAY_PAUSE" }
        val nextIntent = Intent(this, MediaPlayerService::class.java).apply { action = "NEXT" }
        val previousIntent = Intent(this, MediaPlayerService::class.java).apply { action = "PREVIOUS" }
        val closeIntent = Intent(this, MediaPlayerService::class.java).apply { action = "CLOSE" }

        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val nextPendingIntent = PendingIntent.getService(
            this, 1, nextIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val previousPendingIntent = PendingIntent.getService(
            this, 2, previousIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val closePendingIntent = PendingIntent.getService(
            this, 3, closeIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val remainingTimeText = if (timerDuration > 0) {
            val timeLeft = timerDuration - (System.currentTimeMillis() - timerStartTime)
            if (timeLeft > 0) {
                val hours = timeLeft / (1000 * 60 * 60)
                val minutes = (timeLeft % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (timeLeft % (1000 * 60)) / 1000
                localizedContext.getString(
                    R.string.timer_format,
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                )
            } else {
                ""
            }
        } else {
            ""
        }

        val notification = NotificationCompat.Builder(this, "media_player_channel")
            .setContentTitle(playingTitle)
            .setContentText(remainingTimeText)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(localizedContext.resources, R.drawable.logo))
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.baseline_skip_previous_24, "Previous)", previousPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent)
            .addAction(R.drawable.baseline_close_24, "Close", closePendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2, 3)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(1, notification)
    }

    private fun startTimer(duration: Long, startTime: Long = System.currentTimeMillis()) {
        timerJob?.cancel()
        timerStartTime = startTime
        timerDuration = duration
        PirithPrefs.saveTimerState(this, startTime, duration)
        timerJob = coroutineScope.launch {
            var timeLeft = duration
            while (timeLeft > 0 && isActive) {
                timeLeft = duration - (System.currentTimeMillis() - startTime)
                sendUpdateBroadcast(timeLeft)
                updateNotification()
                delay(1000L)
            }
            if (timeLeft <= 0) {
                sendFinishedBroadcast()
                PirithPrefs.clearTimerState(this@MediaPlayerService)
                stop()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerStartTime = 0L
        timerDuration = 0L
        PirithPrefs.clearTimerState(this)
        sendUpdateBroadcast(0L)
        updateNotification()
    }

    private fun sendUpdateBroadcast(remainingTime: Long) {
        val intent = Intent(ACTION_TIMER_UPDATE)
        intent.putExtra(EXTRA_REMAINING_TIME, remainingTime)
        sendBroadcast(intent)
    }

    private fun sendFinishedBroadcast() {
        val intent = Intent(ACTION_TIMER_FINISHED)
        sendBroadcast(intent)
    }

    private fun createLocalizedContext(localeCode: String): Context {
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        return createConfigurationContext(config)
    }

    fun playTrack(trackId: Int) {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        try {
            mediaPlayer = MediaPlayer.create(this, pirithList[trackId].audioResId)
            mediaPlayer?.apply {
                setOnPreparedListener {
                    start()
                    this@MediaPlayerService.isPlaying = true
                    playingTitle = localizedContext.getString(PirithDataProvider.pirithList[trackId].titleResId)
                    isLooping = isRepeatEnabled
                    playingID = trackId
                    PirithPrefs.saveAudioState(this@MediaPlayerService, trackId, true)
                    updateNotification()
                }
                setOnCompletionListener {
                    if (!isRepeatEnabled) {
                        playNextTrack()
                    }
                }
                prepareAsync()
            } ?: run {
                isPlaying = false
                PirithPrefs.saveAudioState(this, playingID, false)
                updateNotification()
            }
        } catch (e: Exception) {
            isPlaying = false
            PirithPrefs.saveAudioState(this, playingID, false)
            updateNotification()
        }
    }

    fun showTrack(trackId: Int) {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        try {
            mediaPlayer = MediaPlayer.create(this, pirithList[trackId].audioResId)
            mediaPlayer?.apply {
                setOnPreparedListener {
                    this@MediaPlayerService.isPlaying = false
                    playingTitle = localizedContext.getString(PirithDataProvider.pirithList[trackId].titleResId)
                    isLooping = isRepeatEnabled
                    playingID = trackId
                    PirithPrefs.saveAudioState(this@MediaPlayerService, trackId, false)
                    updateNotification()
                }
                setOnCompletionListener {
                    if (!isRepeatEnabled) {
                        playNextTrack()
                    }
                }
                prepareAsync()
            } ?: run {
                isPlaying = false
                PirithPrefs.saveAudioState(this, playingID, false)
                updateNotification()
            }
        } catch (e: Exception) {
            isPlaying = false
            PirithPrefs.saveAudioState(this, playingID, false)
            updateNotification()
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.pause()
                    isPlaying = false
                    PirithPrefs.saveAudioState(this, playingID, false)
                } else {
                    it.start()
                    isPlaying = true
                    PirithPrefs.saveAudioState(this, playingID, true)
                }
                updateNotification()
            } catch (e: IllegalStateException) {
                isPlaying = false
                PirithPrefs.saveAudioState(this, playingID, false)
                updateNotification()
            }
        }
    }

    fun playPreviousTrack() {
        val newIndex = if (playingID > 0) playingID - 1 else pirithList.size - 1
        playTrack(newIndex)
    }

    fun playNextTrack() {
        val newIndex = if (isShuffleEnabled) {
            Random.nextInt(pirithList.size)
        } else {
            if (playingID < pirithList.size - 1) playingID + 1 else 0
        }
        playTrack(newIndex)
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        PirithPrefs.saveIsShuffleEnabled(this, isShuffleEnabled)
        updateNotification()
    }

    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
        PirithPrefs.saveIsRepeatEnabled(this, isRepeatEnabled)
        mediaPlayer?.isLooping = isRepeatEnabled
        updateNotification()
    }

    fun getCurrentPosition(): Int = try {
        mediaPlayer?.currentPosition ?: 0
    } catch (e: IllegalStateException) {
        0
    }

    fun getDuration(): Int = try {
        mediaPlayer?.duration ?: 0
    } catch (e: IllegalStateException) {
        0
    }

    fun isPlaying(): Boolean = try {
        mediaPlayer?.isPlaying ?: false
    } catch (e: IllegalStateException) {
        false
    }

    fun getPlayingTitle(): String = playingTitle
    fun getPlayingId(): Int = playingID

    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
        } catch (e: IllegalStateException) {
            // Handle error if needed
        }
    }

    fun stop() {
        try {
            mediaPlayer?.pause()
            isPlaying = false
            PirithPrefs.saveAudioState(this, playingID, false)
            stopTimer()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: IllegalStateException) {
            mediaPlayer = null
            isPlaying = false
            PirithPrefs.saveAudioState(this, playingID, false)
            stopTimer()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        PirithPrefs.saveAudioState(this, playingID, false)
        stopTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        unregisterReceiver(localeChangeReceiver)
    }
}