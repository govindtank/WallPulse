package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.sin

class GradientPulseWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = GradientPulseEngine(this)

    private inner class GradientPulseEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var primaryColor = Color.MAGENTA
        private var secondaryColor = Color.BLUE
        private var darkMode = true
        private var batteryLevel = 100
        private val paint = Paint().apply { isAntiAlias = true }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            batteryLevel = DataRepository.getBatteryLevel(context)
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
            primaryColor = prefs.getInt(PreferenceKeys.PREF_GRADIENT_PRIMARY, Color.MAGENTA)
            secondaryColor = prefs.getInt(PreferenceKeys.PREF_GRADIENT_SECONDARY, Color.BLUE)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                time += 0.01f
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                drawGradient(canvas)
                drawPulse(canvas)
                drawBattery(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawGradient(canvas: Canvas) {
            val gradient = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), primaryColor, secondaryColor, Shader.TileMode.CLAMP)
            paint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null
        }

        private fun drawPulse(canvas: Canvas) {
            val centerX = width / 2f
            val centerY = height / 2f
            val maxRadius = Math.max(width, height).toFloat() / 2
            val pulseRadius = (sin(time * 2) * 0.5f + 0.5f) * maxRadius
            val gradient = RadialGradient(centerX, centerY, pulseRadius, primaryColor, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            paint.shader = gradient
            canvas.drawCircle(centerX, centerY, pulseRadius, paint)
            paint.shader = null
        }

        private fun drawBattery(canvas: Canvas) {
            val cx = width / 2f
            val cy = height - dpToPx(context, 48f)
            paint.color = if (batteryLevel > 20) Color.GREEN else Color.RED
            paint.textSize = dpToPx(context, 14f)
            canvas.drawText("Battery: $batteryLevel%", cx, cy, paint)
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
