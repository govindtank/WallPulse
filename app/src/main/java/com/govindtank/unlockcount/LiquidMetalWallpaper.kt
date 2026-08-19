package com.govindtank.unlockcount

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.cos
import kotlin.math.sin

class LiquidMetalWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = LiquidMetalEngine(this)

    private inner class LiquidMetalEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var metalColor = Color.CYAN
        private var darkMode = true
        private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
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
            metalColor = prefs.getInt(PreferenceKeys.PREF_LIQUID_METAL, Color.CYAN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                time += 0.015f
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                drawBlob(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawBlob(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            val baseRadius = Math.min(width, height) / 3f

            for (i in 0 until 3) {
                val phase = time + i * 2f
                val radius = baseRadius + sin(phase * 3) * baseRadius * 0.3f + cos(phase * 2) * baseRadius * 0.2f
                val gradient = RadialGradient(
                    cx + sin(phase) * 50,
                    cy + cos(phase) * 50,
                    radius,
                    metalColor,
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                paint.alpha = 100
                canvas.drawCircle(cx, cy, radius, paint)
            }
            paint.shader = null
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
