package com.govindtank.unlockcount

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.cos
import kotlin.math.sin

class AuroraWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = AuroraEngine(this)

    private inner class AuroraEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var primaryColor = Color.CYAN
        private var secondaryColor = Color.MAGENTA
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
            primaryColor = prefs.getInt(PreferenceKeys.PREF_AURORA_PRIMARY, Color.CYAN)
            secondaryColor = prefs.getInt(PreferenceKeys.PREF_AURORA_SECONDARY, Color.MAGENTA)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                time += 0.005f
                drawAurora(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawAurora(canvas: Canvas) {
            for (i in 0 until 5) {
                val phase = time + i * 0.5f
                val yOffset = height / 2f + sin(phase) * 100f
                val gradient = LinearGradient(0f, yOffset - 200, width.toFloat(), yOffset + 200, primaryColor, secondaryColor, Shader.TileMode.MIRROR)
                paint.shader = gradient
                paint.alpha = 40
                canvas.drawRect(0f, yOffset - 200, width.toFloat(), yOffset + 200, paint)
            }
            paint.shader = null
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
