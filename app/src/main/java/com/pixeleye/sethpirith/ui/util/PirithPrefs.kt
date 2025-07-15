package com.pixeleye.sethpirith.ui.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton object for managing SharedPreferences in the Pirith app.
 * Handles storage and retrieval of timer, audio, and playback settings.
 */
object PirithPrefs {
    private const val PREF_NAME = "pirith_cache"
    private const val KEY_TIMER_START = "timer_start"
    private const val KEY_TIMER_DURATION = "timer_duration"
    private const val KEY_LAST_AUDIO_ID = "last_audio_id"
    private const val KEY_LAST_AUDIO_TITLE = "last_audio_title"
    private const val KEY_IS_PLAYING = "is_playing"
    private const val KEY_SELECTED_LOCALE = "selected_locale"
    private const val KEY_REPEAT_ENABLED = "is_repeat_enabled"
    private const val KEY_SHUFFLE_ENABLED = "is_shuffle_enabled"

    private val _playingIdFlow = MutableStateFlow(0)
    private val _isPlayingFlow = MutableStateFlow(false)
    var isOnceOpened =  false

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Timer State Management
    /**
     * Saves timer state with start time and duration.
     * @param context Application context
     * @param startTime Timer start time in milliseconds
     * @param duration Timer duration in milliseconds
     */
    fun saveTimerState(context: Context, startTime: Long, duration: Long) {
        getPrefs(context).edit()
            .putLong(KEY_TIMER_START, startTime)
            .putLong(KEY_TIMER_DURATION, duration)
            .apply()
    }

    /**
     * Retrieves timer state as a Pair of start time and duration.
     * @param context Application context
     * @return Pair of start time and duration, or null if invalid
     */
    fun getTimerState(context: Context): Pair<Long, Long>? {
        val prefs = getPrefs(context)
        val startTime = prefs.getLong(KEY_TIMER_START, 0L)
        val duration = prefs.getLong(KEY_TIMER_DURATION, 0L)
        return if (startTime > 0 && duration > 0) startTime to duration else null
    }

    /**
     * Clears timer state from preferences.
     * @param context Application context
     */
    fun clearTimerState(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_TIMER_START)
            .remove(KEY_TIMER_DURATION)
            .apply()
    }

    // Audio State Management
    /**
     * Saves audio playback state.
     * @param context Application context
     * @param audioId ID of the audio
     * @param isPlaying Whether audio is currently playing
     * @param title Optional audio title
     */
    fun saveAudioState(context: Context, audioId: Int, isPlaying: Boolean) {
        getPrefs(context).edit().apply {
            putInt(KEY_LAST_AUDIO_ID, audioId)
            putBoolean(KEY_IS_PLAYING, isPlaying)
            apply()
        }
        _playingIdFlow.value = audioId
        _isPlayingFlow.value = isPlaying
    }

    /**
     * Retrieves the last played audio ID.
     * @param context Application context
     * @return Last audio ID or -1 if not set
     */
    fun getLastAudioId(context: Context): Int =
        getPrefs(context).getInt(KEY_LAST_AUDIO_ID, -1)


    /**
     * Checks if audio is currently playing.
     * @param context Application context
     * @return True if audio is playing, false otherwise
     */
    fun isPlaying(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_IS_PLAYING, false)

    fun playingIdFlow(context: Context): Flow<Int> {
        _playingIdFlow.value = getLastAudioId(context)
        return _playingIdFlow.asStateFlow()
    }

    fun isPlayingFlow(context: Context): Flow<Boolean> {
        _isPlayingFlow.value = isPlaying(context)
        return _isPlayingFlow.asStateFlow()
    }



    // Locale Management
    /**
     * Saves the selected locale code.
     * @param context Application context
     * @param localeCode Locale code to save
     */
    fun saveLocale(context: Context, localeCode: String) {
        getPrefs(context).edit()
            .putString(KEY_SELECTED_LOCALE, localeCode)
            .apply()
    }

    /**
     * Retrieves the selected locale code.
     * @param context Application context
     * @return Locale code, defaults to "en" if not set
     */
    fun getLocale(context: Context): String =
        getPrefs(context).getString(KEY_SELECTED_LOCALE, "en") ?: "en"

    // Playback Settings
    /**
     * Saves repeat mode state.
     * @param context Application context
     * @param isRepeatEnabled Whether repeat is enabled
     */
    fun saveIsRepeatEnabled(context: Context, isRepeatEnabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_REPEAT_ENABLED, isRepeatEnabled)
            .apply()
    }

    /**
     * Checks if repeat mode is enabled.
     * @param context Application context
     * @return True if repeat is enabled, false otherwise
     */
    fun isRepeatEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_REPEAT_ENABLED, false)

    /**
     * Saves shuffle mode state.
     * @param context Application context
     * @param isShuffleEnabled Whether shuffle is enabled
     */
    fun saveIsShuffleEnabled(context: Context, isShuffleEnabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_SHUFFLE_ENABLED, isShuffleEnabled)
            .apply()
    }

    /**
     * Checks if shuffle mode is enabled.
     * @param context Application context
     * @return True if shuffle is enabled, false otherwise
     */
    fun isShuffleEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHUFFLE_ENABLED, false)

    /**
     * Clears all preferences to default values.
     * @param context Application context
     */
    fun clear(context: Context) {
        getPrefs(context).edit().apply {
            putInt(KEY_LAST_AUDIO_ID, -1)
            putBoolean(KEY_IS_PLAYING, false)
            putString(KEY_LAST_AUDIO_TITLE, null)
            putString(KEY_SELECTED_LOCALE, "en")
            putBoolean(KEY_REPEAT_ENABLED, false)
            putBoolean(KEY_SHUFFLE_ENABLED, false)
            apply()
        }
    }
}