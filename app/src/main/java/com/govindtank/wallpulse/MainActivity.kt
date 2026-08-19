package com.govindtank.wallpulse

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.util.ArrayList

class MainActivity : Activity() {

    private lateinit var rvModes: RecyclerView
    private var selectedMode = ModeKeys.MODE_CLASSIC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvModes = findViewById(R.id.rvModes)
        val btnRandom = findViewById<View>(R.id.btnRandom)
        val btnTrending = findViewById<View>(R.id.btnTrending)

        val modes = listOf(
            ModeItem(ModeKeys.MODE_CLASSIC, getString(R.string.mode_classic), getString(R.string.mode_classic_desc), Color.WHITE),
            ModeItem(ModeKeys.MODE_TIME_FLOW, getString(R.string.mode_time_flow), getString(R.string.mode_time_flow_desc), Color.CYAN),
            ModeItem(ModeKeys.MODE_PARTICLE_WAVE, getString(R.string.mode_particle_wave), getString(R.string.mode_particle_wave_desc), Color.GREEN),
            ModeItem(ModeKeys.MODE_GRADIENT_PULSE, getString(R.string.mode_gradient_pulse), getString(R.string.mode_gradient_pulse_desc), Color.MAGENTA),
            ModeItem(ModeKeys.MODE_DATA_STREAM, getString(R.string.mode_data_stream), getString(R.string.mode_data_stream_desc), Color.GREEN),
            ModeItem(ModeKeys.MODE_AURORA, getString(R.string.mode_aurora), getString(R.string.mode_aurora_desc), Color.CYAN),
            ModeItem(ModeKeys.MODE_MATRIX_RAIN, getString(R.string.mode_matrix_rain), getString(R.string.mode_matrix_rain_desc), Color.GREEN),
            ModeItem(ModeKeys.MODE_COSMIC_DUST, getString(R.string.mode_cosmic_dust), getString(R.string.mode_cosmic_dust_desc), Color.MAGENTA),
            ModeItem(ModeKeys.MODE_LIQUID_METAL, getString(R.string.mode_liquid_metal), getString(R.string.mode_liquid_metal_desc), Color.CYAN),
            ModeItem(ModeKeys.MODE_CYBER_GRID, getString(R.string.mode_cyber_grid), getString(R.string.mode_cyber_grid_desc), Color.GREEN),
            ModeItem(ModeKeys.MODE_OCEAN_DEPTH, getString(R.string.mode_ocean_depth), getString(R.string.mode_ocean_depth_desc), Color.BLUE),
            ModeItem(ModeKeys.MODE_NAME_REVEAL, getString(R.string.mode_name_reveal), getString(R.string.mode_name_reveal_desc), Color.YELLOW)
        )

        rvModes.layoutManager = GridLayoutManager(this, 2)
        rvModes.adapter = ModeAdapter(modes) { mode ->
            selectedMode = mode.key
            animateSelection(mode)
            setWallpaper(mode.key)
        }

        btnRandom.setOnClickListener {
            val randomMode = modes.random()
            selectedMode = randomMode.key
            rvModes.smoothScrollToPosition(modes.indexOf(randomMode))
            animateSelection(randomMode)
            setWallpaper(randomMode.key)
        }

        btnTrending.setOnClickListener {
            val trending = modes.takeLast(4)
            val trendingMode = trending.random()
            selectedMode = trendingMode.key
            rvModes.smoothScrollToPosition(modes.indexOf(trendingMode))
            animateSelection(trendingMode)
            setWallpaper(trendingMode.key)
        }
    }

    private fun animateSelection(mode: ModeItem) {
        rvModes.animate().alpha(0.9f).setDuration(80).withEndAction {
            rvModes.animate().alpha(1f).setDuration(120).start()
        }.start()
    }

    private fun setWallpaper(modeKey: String) {
        val component = when (modeKey) {
            ModeKeys.MODE_CLASSIC -> ComponentName(this, UnlockCounterWallpaper::class.java)
            ModeKeys.MODE_TIME_FLOW -> ComponentName(this, TimeFlowWallpaper::class.java)
            ModeKeys.MODE_PARTICLE_WAVE -> ComponentName(this, ParticleWaveWallpaper::class.java)
            ModeKeys.MODE_GRADIENT_PULSE -> ComponentName(this, GradientPulseWallpaper::class.java)
            ModeKeys.MODE_DATA_STREAM -> ComponentName(this, DataStreamWallpaper::class.java)
            ModeKeys.MODE_AURORA -> ComponentName(this, AuroraWallpaper::class.java)
            ModeKeys.MODE_MATRIX_RAIN -> ComponentName(this, MatrixRainWallpaper::class.java)
            ModeKeys.MODE_COSMIC_DUST -> ComponentName(this, CosmicDustWallpaper::class.java)
            ModeKeys.MODE_LIQUID_METAL -> ComponentName(this, LiquidMetalWallpaper::class.java)
            ModeKeys.MODE_CYBER_GRID -> ComponentName(this, CyberGridWallpaper::class.java)
            ModeKeys.MODE_OCEAN_DEPTH -> ComponentName(this, OceanDepthWallpaper::class.java)
            ModeKeys.MODE_NAME_REVEAL -> ComponentName(this, NameRevealWallpaper::class.java)
            else -> ComponentName(this, UnlockCounterWallpaper::class.java)
        }
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        startActivity(intent)
    }
}
