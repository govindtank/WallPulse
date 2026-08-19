package com.govindtank.unlockcount

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.text.TextPaint
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.cos
import kotlin.math.sin

class TimeFlowWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = TimeFlowEngine(this)

    private inner class TimeFlowEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var points = mutableListOf<TimePoint>()
        private var arcColor = Color.WHITE
        private var dotColor = Color.WHITE
        private var use24Hour = true
        private val arcPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }
        private val dotPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val textPaint = TextPaint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            if (isPreview.not()) points.addAll(loadTodayPoints()) else points = generatePreviewPoints().toMutableList()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            textPaint.textSize = dpToPx(context, 12f)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            arcColor = prefs.getInt(PreferenceKeys.TF_KEY_ARC_COLOR, Color.WHITE)
            dotColor = prefs.getInt(PreferenceKeys.TF_KEY_DOT_COLOR, Color.WHITE)
            use24Hour = prefs.getBoolean(PreferenceKeys.TF_KEY_USE_24_HOUR, true)
            arcPaint.color = arcColor
            dotPaint.color = dotColor
            arcPaint.strokeWidth = dpToPx(context, 2f)
        }

        private fun draw() {
            surfaceHolder.lockCanvas()?.apply {
                drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                drawArc(this)
                drawDots(this)
                drawLabels(this)
                surfaceHolder.unlockCanvasAndPost(this)
                handler.postDelayed(drawRunner, 1000)
            }
        }

        private fun drawArc(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f + dpToPx(context, 40f)
            val radius = minOf(width, height) / 2.5f
            canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, 180f, 180f, false, arcPaint)
        }

        private fun drawDots(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f + dpToPx(context, 40f)
            val radius = minOf(width, height) / 2.5f
            for (point in points) {
                val angle = Math.toRadians((point.hour / 24f * 360 - 90).toDouble())
                val x = (cx + radius * cos(angle)).toFloat()
                val y = (cy + radius * sin(angle)).toFloat()
                dotPaint.alpha = (point.intensity * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(x, y, dpToPx(context, 3f), dotPaint)
            }
        }

        private fun drawLabels(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f + dpToPx(context, 40f)
            val radius = minOf(width, height) / 2.5f
            textPaint.color = Color.WHITE
            for (hour in 0..23 step 3) {
                val angle = Math.toRadians((hour / 24f * 360 - 90).toDouble())
                val x = (cx + (radius + dpToPx(context, 16f)) * cos(angle)).toFloat()
                val y = (cy + (radius + dpToPx(context, 16f)) * sin(angle)).toFloat()
                val label = if (use24Hour) hour.toString() else when {
                    hour == 0 -> "12a"
                    hour < 12 -> "${hour}a"
                    hour == 12 -> "12p"
                    else -> "${hour - 12}p"
                }
                canvas.drawText(label, x, y + dpToPx(context, 4f), textPaint)
            }
        }

        private fun loadTodayPoints(): List<TimePoint> {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val historyJson = prefs.getString(PreferenceKeys.HISTORY_PREFERENCE, null)
            val history: List<DailyStat> = if (historyJson != null) {
                val type = object : TypeToken<List<DailyStat>>() {}.type
                Gson().fromJson(historyJson, type)
            } else emptyList()
            val today = java.time.LocalDate.now().toString()
            val todayStat = history.firstOrNull { it.date == today } ?: DailyStat(today, 0)
            return distributePoints(todayStat.count)
        }

        private fun generatePreviewPoints(): List<TimePoint> {
            return listOf(
                TimePoint(8, 0.3f),
                TimePoint(9, 0.5f),
                TimePoint(12, 0.7f),
                TimePoint(18, 0.8f),
                TimePoint(21, 0.7f)
            )
        }

        private fun distributePoints(count: Int): List<TimePoint> {
            val newPoints = mutableListOf<TimePoint>()
            val base = maxOf(count / 3, 3)
            newPoints += TimePoint(8, (base * 0.3f).coerceAtMost(1f))
            newPoints += TimePoint(9, (base * 0.5f).coerceAtMost(1f))
            newPoints += TimePoint(12, (base * 0.7f).coerceAtMost(1f))
            newPoints += TimePoint(18, (base * 0.8f).coerceAtMost(1f))
            newPoints += TimePoint(21, (base * 0.6f).coerceAtMost(1f))
            return newPoints
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}

data class TimePoint(val hour: Int, val intensity: Float)

