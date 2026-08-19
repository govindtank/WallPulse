package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateTimeWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = DateTimeEngine(this)

    private inner class DateTimeEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var darkMode = true
        private var dateFormat = "MMM dd, yyyy"
        private var timeFormat = "HH:mm"
        private var showSeconds = false
        private var fontColor = Color.WHITE
        private var showDate = true
        private val timePaint = TextPaint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
        private val datePaint = TextPaint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
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
            timePaint.textSize = dpToPx(context, 80f)
            datePaint.textSize = dpToPx(context, 28f)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            dateFormat = prefs.getString(PreferenceKeys.DT_KEY_DATE_FORMAT, "MMM dd, yyyy") ?: "MMM dd, yyyy"
            timeFormat = prefs.getString(PreferenceKeys.DT_KEY_TIME_FORMAT, showSeconds.let { if (it) "HH:mm:ss" else "HH:mm" }) ?: "HH:mm"
            showSeconds = prefs.getBoolean(PreferenceKeys.DT_KEY_SHOW_SECONDS, false)
            fontColor = prefs.getInt(PreferenceKeys.DT_KEY_FONT_COLOR, Color.WHITE)
            showDate = prefs.getBoolean(PreferenceKeys.DT_KEY_SHOW_DATE, true)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                drawDateTime(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, if (showSeconds) 1000 else 60000)
        }

        private fun drawDateTime(canvas: Canvas) {
            val now = Date()
            timePaint.color = fontColor
            datePaint.color = fontColor

            timePaint.setShadowLayer(dpToPx(context, 4f), 0f, 0f, if (darkMode) Color.BLACK else Color.WHITE)
            datePaint.setShadowLayer(dpToPx(context, 2f), 0f, 0f, if (darkMode) Color.BLACK else Color.WHITE)

            val timeStr = SimpleDateFormat(if (showSeconds) "HH:mm:ss" else timeFormat, Locale.getDefault()).format(now)
            val dateStr = SimpleDateFormat(dateFormat, Locale.getDefault()).format(now)

            val centerX = width / 2f
            val centerY = height / 2f

            if (showDate) {
                canvas.drawText(dateStr, centerX, centerY - dpToPx(context, 60f), datePaint)
                canvas.drawText(timeStr, centerX, centerY + dpToPx(context, 30f), timePaint)
            } else {
                canvas.drawText(timeStr, centerX, centerY + timePaint.textSize / 3f, timePaint)
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}