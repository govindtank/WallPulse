package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
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
        private var darkMode = true
        private var screenTime = 0
        private var effect = "glow"
        private var glowIntensity = 20
        private var scale = 1.0f
        private var fontFamily = "monospace"
        private val arcPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }
        private val dotPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val textPaint = TextPaint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            if (isPreview.not()) {
                screenTime = DataRepository.getScreenTimeMinutes(context)
                points.addAll(loadTodayPoints())
            } else {
                points = generatePreviewPoints().toMutableList()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            textPaint.textSize = dpToPx(context, 12f * scale)
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
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
            effect = prefs.getString(PreferenceKeys.TF_KEY_EFFECT, "glow") ?: "glow"
            glowIntensity = prefs.getInt(PreferenceKeys.TF_KEY_GLOW_INTENSITY, 20)
            scale = prefs.getInt(PreferenceKeys.TF_KEY_SCALE, 100).toFloat() / 100f
            fontFamily = prefs.getString(PreferenceKeys.TF_KEY_FONT_FAMILY, "monospace") ?: "monospace"
            textPaint.typeface = getFont(fontFamily)
            arcPaint.color = arcColor
            dotPaint.color = dotColor
            arcPaint.strokeWidth = dpToPx(context, 2f * scale)
            textPaint.color = if (darkMode) Color.WHITE else Color.BLACK
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                drawArc(canvas)
                drawDots(canvas)
                drawLabels(canvas)
                drawScreenTime(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 1000)
        }

        private fun drawArc(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f + dpToPx(context, 40f * scale)
            val radius = minOf(width, height) / 2.5f * scale
            val sweepRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            arcPaint.shader = SweepGradient(cx, cy, intArrayOf(arcColor, dotColor, arcColor), null)
            arcPaint.setShadowLayer(dpToPx(context, glowIntensity.toFloat()), 0f, 0f, arcColor)
            canvas.drawArc(sweepRect, 180f, 180f, false, arcPaint)
            arcPaint.shader = null
            arcPaint.clearShadowLayer()
        }

        private fun drawDots(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f + dpToPx(context, 40f * scale)
            val radius = minOf(width, height) / 2.5f * scale
            for (point in points) {
                val angle = Math.toRadians((point.hour / 24f * 360 - 90).toDouble())
                val x = (cx + radius * cos(angle)).toFloat()
                val y = (cy + radius * sin(angle)).toFloat()
                dotPaint.alpha = (point.intensity * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(x, y, dpToPx(context, 4f * scale), dotPaint)
            }
        }

        private fun drawLabels(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f + dpToPx(context, 40f * scale)
            val radius = minOf(width, height) / 2.5f * scale
            textPaint.color = if (darkMode) Color.WHITE else Color.BLACK
            for (hour in 0..23 step 3) {
                val angle = Math.toRadians((hour / 24f * 360 - 90).toDouble())
                val x = (cx + (radius + dpToPx(context, 16f * scale)) * cos(angle)).toFloat()
                val y = (cy + (radius + dpToPx(context, 16f * scale)) * sin(angle)).toFloat()
                val label = if (use24Hour) hour.toString() else when {
                    hour == 0 -> "12a"
                    hour < 12 -> "${hour}a"
                    hour == 12 -> "12p"
                    else -> "${hour - 12}p"
                }
                when (effect) {
                    "neon" -> {
                        textPaint.setShadowLayer(dpToPx(context, glowIntensity.toFloat()), 0f, 0f, textPaint.color)
                        canvas.drawText(label, x, y + dpToPx(context, 4f * scale), textPaint)
                        textPaint.clearShadowLayer()
                        textPaint.strokeWidth = 2f
                        textPaint.style = Paint.Style.STROKE
                        canvas.drawText(label, x, y + dpToPx(context, 4f * scale), textPaint)
                        textPaint.style = Paint.Style.FILL
                    }
                    "glitch" -> {
                        val offset = (System.currentTimeMillis() % 1000 / 100f).toInt() * 2
                        canvas.drawText(label, x + offset, y + dpToPx(context, 4f * scale), textPaint)
                    }
                    else -> {
                        textPaint.setShadowLayer(dpToPx(context, (glowIntensity / 2).toFloat()), 0f, 0f, textPaint.color)
                        canvas.drawText(label, x, y + dpToPx(context, 4f * scale), textPaint)
                        textPaint.clearShadowLayer()
                    }
                }
            }
        }

        private fun drawScreenTime(canvas: Canvas) {
            val cx = width / 2f
            val cy = height - dpToPx(context, 48f * scale)
            textPaint.color = if (darkMode) Color.WHITE else Color.BLACK
            textPaint.textSize = dpToPx(context, 14f * scale)
            when (effect) {
                "neon" -> {
                    textPaint.setShadowLayer(dpToPx(context, glowIntensity.toFloat()), 0f, 0f, textPaint.color)
                    canvas.drawText("Screen time: ${screenTime}m", cx, cy, textPaint)
                    textPaint.clearShadowLayer()
                }
                "glitch" -> {
                    val offset = (System.currentTimeMillis() % 1000 / 100f).toInt() * 2
                    canvas.drawText("Screen time: ${screenTime}m", cx + offset, cy, textPaint)
                }
                else -> {
                    textPaint.setShadowLayer(dpToPx(context, (glowIntensity / 2).toFloat()), 0f, 0f, textPaint.color)
                    canvas.drawText("Screen time: ${screenTime}m", cx, cy, textPaint)
                    textPaint.clearShadowLayer()
                }
            }
        }

        private fun loadTodayPoints(): List<TimePoint> {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val historyJson = prefs.getString(PreferenceKeys.HISTORY_PREFERENCE, null)
            val history: List<DailyStat> = if (!historyJson.isNullOrEmpty()) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<DailyStat>>() {}.type
                val result = gson.fromJson<List<DailyStat>>(historyJson, type)
                result ?: emptyList()
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

data class TimePoint(val hour: Int, val intensity: Float)
