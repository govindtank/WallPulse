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
        private var effect = "glow"
        private var glowIntensity = 20
        private var scale = 1.0f
        private var fontFamily = "monospace"
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
            timePaint.textSize = dpToPx(context, 80f * scale)
            datePaint.textSize = dpToPx(context, 28f * scale)
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
            effect = prefs.getString(PreferenceKeys.DT_KEY_EFFECT, "glow") ?: "glow"
            glowIntensity = prefs.getInt(PreferenceKeys.DT_KEY_GLOW_INTENSITY, 20)
            scale = prefs.getInt(PreferenceKeys.DT_KEY_SCALE, 100).toFloat() / 100f
            fontFamily = prefs.getString(PreferenceKeys.DT_KEY_FONT_FAMILY, "monospace") ?: "monospace"
            timePaint.typeface = getFont(fontFamily)
            datePaint.typeface = getFont(fontFamily)
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

            val timeStr = SimpleDateFormat(if (showSeconds) "HH:mm:ss" else timeFormat, Locale.getDefault()).format(now)
            val dateStr = SimpleDateFormat(dateFormat, Locale.getDefault()).format(now)

            val centerX = width / 2f
            val centerY = height / 2f

            when (effect) {
                "neon" -> drawNeonEffect(canvas, timeStr, dateStr, centerX, centerY)
                "glitch" -> drawGlitchEffect(canvas, timeStr, dateStr, centerX, centerY)
                "shadow" -> drawShadowEffect(canvas, timeStr, dateStr, centerX, centerY)
                else -> drawGlowEffect(canvas, timeStr, dateStr, centerX, centerY)
            }
        }

        private fun drawGlowEffect(canvas: Canvas, timeStr: String, dateStr: String, cx: Float, cy: Float) {
            timePaint.setShadowLayer(dpToPx(context, glowIntensity.toFloat()), 0f, 0f, fontColor)
            datePaint.setShadowLayer(dpToPx(context, (glowIntensity / 2).toFloat()), 0f, 0f, fontColor)
            
            if (showDate) {
                canvas.drawText(dateStr, cx, cy - dpToPx(context, 60f * scale), datePaint)
                canvas.drawText(timeStr, cx, cy + dpToPx(context, 30f * scale), timePaint)
            } else {
                canvas.drawText(timeStr, cx, cy + timePaint.textSize / 3f, timePaint)
            }
            
            timePaint.clearShadowLayer()
            datePaint.clearShadowLayer()
        }

        private fun drawNeonEffect(canvas: Canvas, timeStr: String, dateStr: String, cx: Float, cy: Float) {
            timePaint.setShadowLayer(dpToPx(context, glowIntensity.toFloat()), 0f, 0f, fontColor)
            datePaint.setShadowLayer(dpToPx(context, (glowIntensity / 2).toFloat()), 0f, 0f, fontColor)
            
            if (showDate) {
                canvas.drawText(dateStr, cx, cy - dpToPx(context, 60f * scale), datePaint)
                canvas.drawText(timeStr, cx, cy + dpToPx(context, 30f * scale), timePaint)
            } else {
                canvas.drawText(timeStr, cx, cy + timePaint.textSize / 3f, timePaint)
            }
            
            timePaint.clearShadowLayer()
            datePaint.clearShadowLayer()
            
            // Neon outline
            timePaint.strokeWidth = 2f
            timePaint.style = Paint.Style.STROKE
            if (showDate) {
                canvas.drawText(timeStr, cx, cy + dpToPx(context, 30f * scale), timePaint)
            } else {
                canvas.drawText(timeStr, cx, cy + timePaint.textSize / 3f, timePaint)
            }
            timePaint.style = Paint.Style.FILL
        }

        private fun drawGlitchEffect(canvas: Canvas, timeStr: String, dateStr: String, cx: Float, cy: Float) {
            val glitchOffset = (System.currentTimeMillis() % 1000 / 100f).toInt() * 2
            
            if (showDate) {
                canvas.drawText(dateStr, cx + glitchOffset, cy - dpToPx(context, 60f * scale), datePaint)
                canvas.drawText(timeStr, cx - glitchOffset, cy + dpToPx(context, 30f * scale), timePaint)
            } else {
                canvas.drawText(timeStr, cx - glitchOffset, cy + timePaint.textSize / 3f, timePaint)
            }
        }

        private fun drawShadowEffect(canvas: Canvas, timeStr: String, dateStr: String, cx: Float, cy: Float) {
            timePaint.setShadowLayer(dpToPx(context, 4f), 2f, 2f, Color.GRAY)
            datePaint.setShadowLayer(dpToPx(context, 2f), 1f, 1f, Color.GRAY)
            
            if (showDate) {
                canvas.drawText(dateStr, cx, cy - dpToPx(context, 60f * scale), datePaint)
                canvas.drawText(timeStr, cx, cy + dpToPx(context, 30f * scale), timePaint)
            } else {
                canvas.drawText(timeStr, cx, cy + timePaint.textSize / 3f, timePaint)
            }
            
            timePaint.clearShadowLayer()
            datePaint.clearShadowLayer()
        }

        private fun getFont(family: String): Typeface {
            return when (family) {
                "monospace" -> Typeface.MONOSPACE
                "sans_serif" -> Typeface.SANS_SERIF
                "serif" -> Typeface.SERIF
                else -> Typeface.create(family, Typeface.NORMAL)
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
