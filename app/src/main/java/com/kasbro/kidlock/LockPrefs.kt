package com.kasbro.kidlock

import android.content.Context

object LockPrefs {
    private const val PREFS = "kidlock_state"
    private const val KEY_END_TIME = "end_time"
    private const val KEY_TIMER_RUNNING = "timer_running"
    private const val KEY_LOCKED = "is_locked"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setEndTime(context: Context, endTime: Long) {
        prefs(context).edit().putLong(KEY_END_TIME, endTime).putBoolean(KEY_TIMER_RUNNING, true).apply()
    }

    fun getEndTime(context: Context): Long = prefs(context).getLong(KEY_END_TIME, 0L)

    fun isTimerRunning(context: Context): Boolean = prefs(context).getBoolean(KEY_TIMER_RUNNING, false)

    fun setTimerRunning(context: Context, running: Boolean) {
        prefs(context).edit().putBoolean(KEY_TIMER_RUNNING, running).apply()
    }

    fun setLocked(context: Context, locked: Boolean) {
        prefs(context).edit().putBoolean(KEY_LOCKED, locked).apply()
    }

    fun isLocked(context: Context): Boolean = prefs(context).getBoolean(KEY_LOCKED, false)
}
