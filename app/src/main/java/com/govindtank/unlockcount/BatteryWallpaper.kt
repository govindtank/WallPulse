package com.govindtank.unlockcount

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager

class BatteryWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = BatteryEngine(this)

    private inner class BatteryEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var primaryColor = Color.GREEN
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
            primaryColor = prefs.getInt(PreferenceKeys.PREF_BATTERY_PRIMARY, Color.GREEN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                time += 0.01f
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                drawBattery(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawBattery(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            val barWidth = width * 0.6f
            val barHeight = 24f
            val radius = 12f

            paint.color = if (darkMode) Color.WHITE else Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            val left = cx - barWidth / 2
            val top = cy - barHeight / 2
            val right = left + barWidth
            val bottom = top + barHeight
            canvas.drawRoundRect(RectF(left, top, right, bottom), radius, radius, paint)

            val fillWidth = barWidth * (batteryLevel / 100f)
            val fillLeft = left + 2
            val fillTop = top + 2
            val fillRight = fillLeft + fillWidth - 4
            val fillBottom = bottom - 2
            val fill = RectF(fillLeft, fillTop, fillRight, fillBottom)

            val gradient = LinearGradient(fillLeft, 0f, fillRight, 0f, primaryColor, Color.GRAY, Shader.TileMode.CLAMP)
            paint.shader = gradient
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(fill, radius, radius, paint)
            paint.shader = null

            paint.textSize = dpToPx(context, 14f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("$batteryLevel%", cx, bottom + dpToPx(context, 24f), paint)
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
