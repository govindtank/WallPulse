package com.govindtank.wallpulse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.util.Date
import java.util.Calendar
import java.util.TimeZone

object UnlockBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val dayEndLong = prefs.getLong(PreferenceKeys.DAY_END_MS_PREFERENCE, 0)
            val now = Date.from(java.time.Instant.now())
            if (now.time > dayEndLong) {
                val prevCount = prefs.getInt(PreferenceKeys.COUNT_PREFERENCE, PreferenceKeys.COUNT_PREFERENCE_DEFAULT_VALUE)
                prefs.edit().apply {
                    putInt(PreferenceKeys.PREV_COUNT_PREFERENCE, prevCount)
                    putInt(PreferenceKeys.COUNT_PREFERENCE, 1)
                    putLong(PreferenceKeys.DAY_END_MS_PREFERENCE, endOfDay(now))
                    apply()
                }
                saveTodayStat(prefs, 1)
            } else {
                val newCount = prefs.getInt(PreferenceKeys.COUNT_PREFERENCE, PreferenceKeys.COUNT_PREFERENCE_DEFAULT_VALUE) + 1
                prefs.edit().apply {
                    putInt(PreferenceKeys.PREV_COUNT_PREFERENCE, newCount - 1)
                    putInt(PreferenceKeys.COUNT_PREFERENCE, newCount)
                    apply()
                }
                saveTodayStat(prefs, newCount)
            }
        }
    }

    private fun saveTodayStat(prefs: SharedPreferences, count: Int) {
        val today = LocalDate.now().toString()
        val historyJson = prefs.getString(PreferenceKeys.HISTORY_PREFERENCE, null)
        val history: MutableList<DailyStat> = if (historyJson != null) {
            val type = object : TypeToken<MutableList<DailyStat>>() {}.type
            Gson().fromJson(historyJson, type)
        } else {
            ArrayList()
        }
        val existingIndex = history.indexOfFirst { it.date == today }
        if (existingIndex >= 0) {
            history[existingIndex] = DailyStat(today, count)
        } else {
            history.add(DailyStat(today, count))
        }
        prefs.edit().putString(PreferenceKeys.HISTORY_PREFERENCE, Gson().toJson(history)).apply()
    }

    private fun endOfDay(date: Date): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
