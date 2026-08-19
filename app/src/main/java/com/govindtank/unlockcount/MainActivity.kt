package com.govindtank.unlockcount

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.govindtank.unlockcount.ui.settings.AuroraSettingsActivity
import com.govindtank.unlockcount.ui.settings.DataStreamSettingsActivity
import com.govindtank.unlockcount.ui.settings.GradientPulseSettingsActivity
import com.govindtank.unlockcount.ui.settings.MatrixRainSettingsActivity
import com.govindtank.unlockcount.ui.settings.ParticleWaveSettingsActivity
import com.govindtank.unlockcount.ui.settings.TimeFlowSettingsActivity
import com.govindtank.unlockcount.ui.settings.UnlockCountSettingsActivity
import java.time.LocalDate
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var tvTodayCount: TextView
    private lateinit var tvAverage: TextView
    private lateinit var tvBestDay: TextView
    private lateinit var tvStreak: TextView
    private lateinit var rvModes: RecyclerView
    private lateinit var previewImage: ImageView
    private lateinit var tvModeTitle: TextView
    private lateinit var tvModeDesc: TextView
    private lateinit var btnSetWallpaper: View
    private var selectedMode = ModeKeys.MODE_CLASSIC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTodayCount = findViewById(R.id.tvTodayCount)
        tvAverage = findViewById(R.id.tvAverage)
        tvBestDay = findViewById(R.id.tvBestDay)
        tvStreak = findViewById(R.id.tvStreak)
        previewImage = findViewById(R.id.previewImage)
        tvModeTitle = findViewById(R.id.tvModeTitle)
        tvModeDesc = findViewById(R.id.tvModeDesc)
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper)
        rvModes = findViewById(R.id.rvModes)

        val modes = listOf(
            ModeItem(ModeKeys.MODE_CLASSIC, getString(R.string.mode_classic), getString(R.string.mode_classic_desc), Color.WHITE),
            ModeItem(ModeKeys.MODE_TIME_FLOW, getString(R.string.mode_time_flow), getString(R.string.mode_time_flow_desc), Color.CYAN),
            ModeItem(ModeKeys.MODE_PARTICLE_WAVE, getString(R.string.mode_particle_wave), getString(R.string.mode_particle_wave_desc), Color.GREEN),
            ModeItem(ModeKeys.MODE_GRADIENT_PULSE, getString(R.string.mode_gradient_pulse), getString(R.string.mode_gradient_pulse_desc), Color.MAGENTA),
            ModeItem(ModeKeys.MODE_DATA_STREAM, getString(R.string.mode_data_stream), getString(R.string.mode_data_stream_desc), Color.GREEN),
            ModeItem(ModeKeys.MODE_AURORA, getString(R.string.mode_aurora), getString(R.string.mode_aurora_desc), Color.CYAN),
            ModeItem(ModeKeys.MODE_MATRIX_RAIN, getString(R.string.mode_matrix_rain), getString(R.string.mode_matrix_rain_desc), Color.GREEN)
        )

        rvModes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvModes.adapter = ModeAdapter(modes) { mode ->
            selectedMode = mode.key
            tvModeTitle.text = mode.title
            tvModeDesc.text = mode.description
            previewImage.setBackgroundColor(mode.color)
        }

        btnSetWallpaper.setOnClickListener {
            val component = when (selectedMode) {
                ModeKeys.MODE_CLASSIC -> ComponentName(this, UnlockCounterWallpaper::class.java)
                ModeKeys.MODE_TIME_FLOW -> ComponentName(this, TimeFlowWallpaper::class.java)
                ModeKeys.MODE_PARTICLE_WAVE -> ComponentName(this, ParticleWaveWallpaper::class.java)
                ModeKeys.MODE_GRADIENT_PULSE -> ComponentName(this, GradientPulseWallpaper::class.java)
                ModeKeys.MODE_DATA_STREAM -> ComponentName(this, DataStreamWallpaper::class.java)
                ModeKeys.MODE_AURORA -> ComponentName(this, AuroraWallpaper::class.java)
                ModeKeys.MODE_MATRIX_RAIN -> ComponentName(this, MatrixRainWallpaper::class.java)
                else -> ComponentName(this, UnlockCounterWallpaper::class.java)
            }
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            val intent = when (selectedMode) {
                ModeKeys.MODE_TIME_FLOW -> Intent(this, TimeFlowSettingsActivity::class.java)
                ModeKeys.MODE_PARTICLE_WAVE -> Intent(this, ParticleWaveSettingsActivity::class.java)
                ModeKeys.MODE_GRADIENT_PULSE -> Intent(this, GradientPulseSettingsActivity::class.java)
                ModeKeys.MODE_DATA_STREAM -> Intent(this, DataStreamSettingsActivity::class.java)
                ModeKeys.MODE_AURORA -> Intent(this, AuroraSettingsActivity::class.java)
                ModeKeys.MODE_MATRIX_RAIN -> Intent(this, MatrixRainSettingsActivity::class.java)
                else -> Intent(this, UnlockCountSettingsActivity::class.java)
            }
            startActivity(intent)
        }

        if (savedInstanceState == null) {
            selectedMode = ModeKeys.MODE_CLASSIC
            tvModeTitle.text = getString(R.string.mode_classic)
            tvModeDesc.text = getString(R.string.mode_classic_desc)
        }

        loadClassicStats()
    }

    override fun onResume() {
        super.onResume()
        loadClassicStats()
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
