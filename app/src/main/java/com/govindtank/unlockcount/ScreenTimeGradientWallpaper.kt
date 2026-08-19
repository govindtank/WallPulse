package com.govindtank.unlockcount

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.sin

class ScreenTimeGradientWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = ScreenTimeGradientEngine(this)

    private inner class ScreenTimeGradientEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var primaryColor = Color.BLUE
        private var secondaryColor = Color.GREEN
        private var darkMode = true
        private var screenTime = 0
        private val paint = Paint().apply { isAntiAlias = true }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            screenTime = DataRepository.getScreenTimeMinutes(context)
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
            primaryColor = prefs.getInt(PreferenceKeys.PREF_SCREEN_TIME_PRIMARY, Color.BLUE)
            secondaryColor = prefs.getInt(PreferenceKeys.PREF_SCREEN_TIME_SECONDARY, Color.GREEN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                time += 0.01f
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                val gradient = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), primaryColor, secondaryColor, Shader.TileMode.CLAMP)
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
                drawScreenTime(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawScreenTime(canvas: Canvas) {
            val cx = width / 2f
            val cy = height - dpToPx(context, 48f)
            paint.color = if (darkMode) Color.WHITE else Color.BLACK
            paint.textSize = dpToPx(context, 14f)
            canvas.drawText("Screen time: ${screenTime}m", cx, cy, paint)
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
