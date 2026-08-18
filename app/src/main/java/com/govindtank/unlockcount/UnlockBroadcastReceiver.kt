
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


            val dayEndLong = prefs.getLong(DAY_END_MS_PREFERENCE, 0)

            val now = Date.from(Instant.now())
            if (now.time > dayEndLong) {

                prefs.getInt(
                    COUNT_PREFERENCE,
                    COUNT_PREFERENCE_DEFAULT_VALUE
                ).also { count ->
                    prefs.edit().apply {
                        putInt(PREV_COUNT_PREFERENCE, count)
                        putInt(COUNT_PREFERENCE, 1)
                        putLong(
                            DAY_END_MS_PREFERENCE,
                            endOfDay(now)
                        )
                    }.apply()
                }
            }
            else {
                prefs.getInt(
                    COUNT_PREFERENCE,
                    COUNT_PREFERENCE_DEFAULT_VALUE
                ).also { count ->
                    prefs.edit().apply {
                        putInt(PREV_COUNT_PREFERENCE, count)
                        val newCount = count + 1
                        putInt(COUNT_PREFERENCE, newCount).apply()
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
