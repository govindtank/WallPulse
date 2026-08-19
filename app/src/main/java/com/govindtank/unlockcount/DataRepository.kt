package com.govindtank.unlockcount

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.preference.PreferenceManager

object DataRepository {

    fun getUnlockCount(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(PreferenceKeys.COUNT_PREFERENCE, 0)
    }

    fun getScreenTimeMinutes(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt("screen_time_minutes", 0)
    }

    fun getNotificationCount(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt("notification_count", 0)
    }

    fun getBatteryLevel(context: Context): Int {
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100) / scale else 100
    }

    fun getStepCount(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt("step_count", 0)
    }

    fun setScreenTimeMinutes(context: Context, minutes: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt("screen_time_minutes", minutes).apply()
    }

    fun setNotificationCount(context: Context, count: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt("notification_count", count).apply()
    }

    fun setStepCount(context: Context, count: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt("step_count", count).apply()
    }
}
