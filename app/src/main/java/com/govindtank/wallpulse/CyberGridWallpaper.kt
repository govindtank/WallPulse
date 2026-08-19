package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.cos
import kotlin.math.sin

class CyberGridWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = CyberGridEngine(this)

    private inner class CyberGridEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var gridColor = Color.CYAN
        private var darkMode = true
        private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            gridColor = prefs.getInt(PreferenceKeys.PREF_CYBER_GRID, Color.CYAN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                time += 0.02f
                drawGrid(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawGrid(canvas: Canvas) {
            paint.color = gridColor
            paint.strokeWidth = 1f
            paint.alpha = 120

            val horizon = height / 2f
            val vanishX = width / 2f
            val spacing = 60f

            for (i in -20..20) {
                val x = vanishX + i * spacing
                canvas.drawLine(x, height.toFloat(), vanishX, horizon, paint)
            }

            for (i in 0 until 20) {
                val y = horizon + (i / 20f) * (height - horizon)
                val perspective = (y - horizon) / (height - horizon)
                val offset = (time * 50 * perspective) % spacing
                for (j in -20..20) {
                    val x = vanishX + (j * spacing + offset) * perspective
                    if (x >= 0 && x <= width) {
                        canvas.drawLine(x, y, x + spacing * perspective, y, paint)
                    }
                }
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
