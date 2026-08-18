package com.govindtank.unlockcount

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import java.time.Instant
import java.util.*

object UnlockBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val dayEndLong = prefs.getLong(PreferenceKeys.DAY_END_MS_PREFERENCE, 0)
            val now = Date.from(Instant.now())
            if (now.time > dayEndLong) {
                prefs.getInt(PreferenceKeys.COUNT_PREFERENCE, PreferenceKeys.COUNT_PREFERENCE_DEFAULT_VALUE).also { count ->
                    prefs.edit().apply {
                        putInt(PreferenceKeys.PREV_COUNT_PREFERENCE, count)
                        putInt(PreferenceKeys.COUNT_PREFERENCE, 1)
                        putLong(PreferenceKeys.DAY_END_MS_PREFERENCE, endOfDay(now))
                    }.apply()
                }
            } else {
                prefs.getInt(PreferenceKeys.COUNT_PREFERENCE, PreferenceKeys.COUNT_PREFERENCE_DEFAULT_VALUE).also { count ->
                    prefs.edit().apply {
                        putInt(PreferenceKeys.PREV_COUNT_PREFERENCE, count)
                        putInt(PreferenceKeys.COUNT_PREFERENCE, count + 1).apply()
                    }
                }
            }
        }
    }

    private fun endOfDay(date: Date): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
