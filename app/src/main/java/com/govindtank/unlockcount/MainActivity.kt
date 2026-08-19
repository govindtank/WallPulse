package com.govindtank.unlockcount

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.govindtank.unlockcount.ui.settings.TimeFlowSettingsActivity
import com.govindtank.unlockcount.ui.settings.UnlockCountSettingsActivity
import java.time.LocalDate
import java.util.ArrayList

class MainActivity : Activity() {

    private lateinit var tvTodayCount: TextView
    private lateinit var tvAverage: TextView
    private lateinit var tvBestDay: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tabModes: TabLayout
    private lateinit var classicDashboard: View
    private lateinit var timeFlowDashboard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTodayCount = findViewById(R.id.tvTodayCount)
        tvAverage = findViewById(R.id.tvAverage)
        tvBestDay = findViewById(R.id.tvBestDay)
        tvStreak = findViewById(R.id.tvStreak)
        classicDashboard = findViewById(R.id.classicStats)
        timeFlowDashboard = findViewById(R.id.timeFlowStats)
        tabModes = findViewById(R.id.tabModes)

        tabModes.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showClassicDashboard()
                    1 -> showTimeFlowDashboard()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val btnSetWallpaper = findViewById<Button>(R.id.btnSetWallpaper)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        btnSetWallpaper.setOnClickListener {
            val selectedMode = if (tabModes.selectedTabPosition == 1) ModeKeys.MODE_TIME_FLOW else ModeKeys.MODE_CLASSIC
            val component = when (selectedMode) {
                ModeKeys.MODE_TIME_FLOW -> ComponentName(this, TimeFlowWallpaper::class.java)
                else -> ComponentName(this, UnlockCounterWallpaper::class.java)
            }
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val selectedMode = if (tabModes.selectedTabPosition == 1) ModeKeys.MODE_TIME_FLOW else ModeKeys.MODE_CLASSIC
            val intent = when (selectedMode) {
                ModeKeys.MODE_TIME_FLOW -> Intent(this, TimeFlowSettingsActivity::class.java)
                else -> Intent(this, UnlockCountSettingsActivity::class.java)
            }
            startActivity(intent)
        }

        showClassicDashboard()
    }

    override fun onResume() {
        super.onResume()
        val selectedMode = if (tabModes.selectedTabPosition == 1) ModeKeys.MODE_TIME_FLOW else ModeKeys.MODE_CLASSIC
        when (selectedMode) {
            ModeKeys.MODE_TIME_FLOW -> showTimeFlowDashboard()
            else -> showClassicDashboard()
        }
    }

    private fun showClassicDashboard() {
        classicDashboard.visibility = View.VISIBLE
        timeFlowDashboard.visibility = View.GONE
        loadClassicStats()
    }

    private fun showTimeFlowDashboard() {
        classicDashboard.visibility = View.GONE
        timeFlowDashboard.visibility = View.VISIBLE
    }

    private fun loadClassicStats() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val todayCount = prefs.getInt(PreferenceKeys.COUNT_PREFERENCE, 0)
        val historyJson = prefs.getString(PreferenceKeys.HISTORY_PREFERENCE, null)
        val history: MutableList<DailyStat> = if (historyJson != null) {
            Gson().fromJson(historyJson, object : TypeToken<MutableList<DailyStat>>() {}.type)
        } else {
            ArrayList()
        }

        val today = LocalDate.now()
        val historyWithToday = history.toMutableList()
        val existingIndex = historyWithToday.indexOfFirst { it.date == today.toString() }
        if (existingIndex >= 0) {
            historyWithToday[existingIndex] = DailyStat(today.toString(), todayCount)
        } else {
            historyWithToday.add(DailyStat(today.toString(), todayCount))
        }

        val last7 = historyWithToday.takeLast(7)
        val average = if (last7.isNotEmpty()) last7.map { it.count }.average().toInt() else 0
        val bestDay = if (last7.isNotEmpty()) last7.maxBy { it.count }.count else 0
        val streak = calculateStreak(historyWithToday)

        tvTodayCount.text = todayCount.toString()
        tvAverage.text = average.toString()
        tvBestDay.text = bestDay.toString()
        tvStreak.text = streak.toString()
    }

    private fun calculateStreak(history: List<DailyStat>): Int {
        if (history.isEmpty()) return 0
        val today = LocalDate.now()
        var streak = 0
        var current = today
        for (stat in history.asReversed()) {
            val statDate = LocalDate.parse(stat.date)
            if (statDate == current && stat.count > 0) {
                streak++
                current = current.minusDays(1)
            } else if (statDate == current && stat.count == 0) {
                break
            } else if (statDate.isBefore(current)) {
                current = current.minusDays(1)
            }
        }
        return streak
    }
}
