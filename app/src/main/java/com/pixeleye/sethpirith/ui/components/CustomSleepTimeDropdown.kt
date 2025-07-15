package com.pixeleye.sethpirith.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pixeleye.sethpirith.R
import com.pixeleye.sethpirith.service.MediaPlayerService
import com.pixeleye.sethpirith.ui.util.PirithPrefs
import java.util.concurrent.TimeUnit

@Composable
fun CustomSleepTimeDropdown() {
    val context = LocalContext.current

    // Data class to hold display text and duration
    data class SleepTimeOption(val displayText: String, val durationMillis: Long, val key: String)

    // Define sleep time options with keys for mapping
    val sleepTimeOptions = remember {
        listOf(
            SleepTimeOption(
                displayText = context.getString(R.string.label_5m),
                durationMillis = 5 * 60 * 1000L,
                key = "5_minutes"
            ),
            SleepTimeOption(
                displayText = context.getString(R.string.label_10m),
                durationMillis = 10 * 60 * 1000L,
                key = "10_minutes"
            ),
            SleepTimeOption(
                displayText = context.getString(R.string.label_15m),
                durationMillis = 15 * 60 * 1000L,
                key = "15_minutes"
            ),
            SleepTimeOption(
                displayText = context.getString(R.string.label_30m),
                durationMillis = 30 * 60 * 1000L,
                key = "30_minutes"
            ),
            SleepTimeOption(
                displayText = context.getString(R.string.label_1h),
                durationMillis = 60 * 60 * 1000L,
                key = "1_hour"
            ),
            SleepTimeOption(
                displayText = context.getString(R.string.label_never),
                durationMillis = 0L,
                key = "never"
            )
        )
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedSleepTime by remember { mutableStateOf(sleepTimeOptions[5]) }
    var remainingTime by remember { mutableStateOf<Long?>(null) }

    // State to trigger recomposition on locale change
    var localeChanged by remember { mutableStateOf(0) }

    // BroadcastReceiver for locale changes
    val localeReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_LOCALE_CHANGED) {
                    localeChanged++ // Trigger recomposition
                }
            }
        }
    }
    fun onTimerFinished(){
        // Exit the current process
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    // BroadcastReceiver for timer updates
    val timerReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    MediaPlayerService.ACTION_TIMER_UPDATE -> {
                        val timeLeft = intent.getLongExtra(MediaPlayerService.EXTRA_REMAINING_TIME, 0L)
                        remainingTime = if (timeLeft > 0) timeLeft else null
                    }
                    MediaPlayerService.ACTION_TIMER_FINISHED -> {
                        remainingTime = null
                        selectedSleepTime = sleepTimeOptions.find { it.key == "never" } ?: sleepTimeOptions[5]
                        onTimerFinished()
                    }
                }
            }
        }
    }

    // Register BroadcastReceivers
    DisposableEffect(Unit) {
        val localeFilter = IntentFilter(Intent.ACTION_LOCALE_CHANGED)
        ContextCompat.registerReceiver(
            context,
            localeReceiver,
            localeFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val timerFilter = IntentFilter().apply {
            addAction(MediaPlayerService.ACTION_TIMER_UPDATE)
            addAction(MediaPlayerService.ACTION_TIMER_FINISHED)
        }
        ContextCompat.registerReceiver(
            context,
            timerReceiver,
            timerFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Restore timer state
        PirithPrefs.getTimerState(context)?.let { (startTime, duration) ->
            val timeElapsed = System.currentTimeMillis() - startTime
            val timeLeft = duration - timeElapsed
            if (timeLeft > 0) {
                remainingTime = timeLeft
                selectedSleepTime = sleepTimeOptions.find {
                    it.durationMillis == duration
                } ?: sleepTimeOptions[5]
            } else {
                PirithPrefs.clearTimerState(context)
                context.stopService(Intent(context, MediaPlayerService::class.java))
            }
        }

        onDispose {
            context.unregisterReceiver(localeReceiver)
            context.unregisterReceiver(timerReceiver)
        }
    }

    // Format remaining time as HH:MM:SS
    fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    // Start/stop timer in MediaPlayerService
    fun startTimer(option: SleepTimeOption) {
        if (option.key != "never") {
            val intent = Intent(context, MediaPlayerService::class.java).apply {
                action = MediaPlayerService.ACTION_START_TIMER
                putExtra(MediaPlayerService.EXTRA_DURATION, option.durationMillis)
            }
            context.startService(intent)
        } else {
            val intent = Intent(context, MediaPlayerService::class.java).apply {
                action = MediaPlayerService.ACTION_STOP_TIMER
            }
            context.startService(intent)
        }
    }

    // Recompose sleepTimes when locale changes
    val currentSleepTimes by remember(localeChanged) {
        mutableStateOf(
            sleepTimeOptions.map { option ->
                option.copy(
                    displayText = when (option.key) {
                        "5_minutes" -> context.getString(R.string.label_5m)
                        "10_minutes" -> context.getString(R.string.label_10m)
                        "15_minutes" -> context.getString(R.string.label_15m)
                        "30_minutes" -> context.getString(R.string.label_30m)
                        "1_hour" -> context.getString(R.string.label_1h)
                        "never" -> context.getString(R.string.label_never)
                        else -> option.displayText
                    }
                )
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        // Main button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(1.dp, Color.White, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedSleepTime.displayText,
                    color = Color.White
                )
                remainingTime?.let {
                    Text(
                        text = formatTime(it),
                        color = Color.White
                    )
                }
            }
        }

        // Dropdown content
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            currentSleepTimes.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayText, color = Color.White) },
                    onClick = {
                        selectedSleepTime = option
                        expanded = false
                        startTimer(option)
                    }
                )
            }
        }
    }
}