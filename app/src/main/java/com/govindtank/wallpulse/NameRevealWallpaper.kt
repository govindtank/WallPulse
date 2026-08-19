package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.random.Random

class NameRevealWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = NameRevealEngine(this)

    private enum class Phase { RAIN, REVEAL, HOLD, RESET }

    private inner class NameRevealEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var columns = 0
        private var drops = mutableListOf<Float>()
        private var darkMode = true
        private var customName = "Govind"
        private var matrixColor = Color.GREEN
        private var revealColor = Color.WHITE
        private var fontSize = 24f
        private var fontFamily = "monospace"
        private var revealDuration = 3000L
        private var holdDuration = 30000L
        private var columnSpacing = 30f
        private var startTime = 0L
        private var phase = Phase.RAIN
        private var currentCharIndex = 0
        private var charRevealStart = 0L
        private var charDecoyDuration = 600L
        private var settledAlpha = 255
        private var paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val matrixChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#$%&*"
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            initColumns()
            startTime = System.currentTimeMillis()
            phase = Phase.RAIN
            currentCharIndex = 0
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                startTime = System.currentTimeMillis()
                phase = Phase.RAIN
                currentCharIndex = 0
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            initColumns()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun initColumns() {
            if (width <= 0 || height <= 0) return
            columns = (width / columnSpacing).toInt().coerceAtLeast(1)
            drops = mutableListOf<Float>().apply {
                for (i in 0 until columns) add(Random.nextFloat() * height)
            }
        }

        private fun loadPrefs() {
            try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                customName = prefs.getString(PreferenceKeys.NAME_REVEAL_NAME, "Govind") ?: "Govind"
                matrixColor = try { Color.parseColor(prefs.getString(PreferenceKeys.NAME_REVEAL_MATRIX_COLOR, "#00FF88") ?: "#00FF88") } catch (e: Exception) { Color.GREEN }
                revealColor = try { Color.parseColor(prefs.getString(PreferenceKeys.NAME_REVEAL_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF") } catch (e: Exception) { Color.WHITE }
                fontSize = prefs.getInt(PreferenceKeys.NAME_REVEAL_FONT_SIZE, 24).coerceIn(8, 120).toFloat()
                fontFamily = prefs.getString(PreferenceKeys.NAME_REVEAL_FONT_FAMILY, "monospace") ?: "monospace"
                revealDuration = prefs.getInt(PreferenceKeys.NAME_REVEAL_DURATION, 3000).coerceIn(500, 30000).toLong()
                holdDuration = prefs.getInt(PreferenceKeys.NAME_REVEAL_HOLD_DURATION, 30000).coerceIn(5000, 120000).toLong()
                columnSpacing = prefs.getInt(PreferenceKeys.NAME_REVEAL_COLUMN_SPACING, 30).coerceIn(10, 200).toFloat()
                charDecoyDuration = prefs.getInt(PreferenceKeys.NAME_REVEAL_DECOY_DURATION, 600).coerceIn(100, 3000).toLong()
                darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
            } catch (e: Exception) {
                customName = "Govind"
                matrixColor = Color.GREEN
                revealColor = Color.WHITE
                fontSize = 24f
                fontFamily = "monospace"
                revealDuration = 3000L
                holdDuration = 30000L
                columnSpacing = 30f
                charDecoyDuration = 600L
                darkMode = true
            }
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                updatePhase()
                drawMatrixRain(canvas)
                if (phase == Phase.REVEAL || phase == Phase.HOLD) {
                    drawNameReveal(canvas)
                }
            } catch (e: Exception) {
                canvas.drawColor(Color.BLACK)
            } finally {
                try { surfaceHolder.unlockCanvasAndPost(canvas) } catch (e: Exception) {}
            }
            handler.postDelayed(drawRunner, 50)
        }

        private fun updatePhase() {
            val now = System.currentTimeMillis()
            val elapsed = now - startTime

            when (phase) {
                Phase.RAIN -> {
                    if (elapsed >= revealDuration) {
                        phase = Phase.REVEAL
                        currentCharIndex = 0
                        charRevealStart = now
                    }
                }
                Phase.REVEAL -> {
                    val revealElapsed = now - charRevealStart
                    val charsToReveal = (revealElapsed / (charDecoyDuration + 80)).toInt()
                    if (charsToReveal >= customName.length) {
                        currentCharIndex = customName.length
                        phase = Phase.HOLD
                        startTime = now
                    } else {
                        currentCharIndex = charsToReveal.coerceIn(0, customName.length)
                    }
                }
                Phase.HOLD -> {
                    if (now - startTime >= holdDuration) {
                        phase = Phase.RESET
                        startTime = now
                    }
                }
                Phase.RESET -> {
                    if (now - startTime >= 1500) {
                        phase = Phase.RAIN
                        currentCharIndex = 0
                        startTime = now
                    }
                }
            }
        }

        private fun drawMatrixRain(canvas: Canvas) {
            if (width <= 0 || height <= 0) return
            paint.color = matrixColor
            paint.alpha = 180
            paint.textSize = fontSize.coerceAtLeast(8f)
            paint.typeface = getFont(fontFamily)

            for (i in 0 until columns) {
                val x = i * columnSpacing
                val y = drops[i]
                var ch = (Random.nextInt(33, 127)).toChar()
                if (phase == Phase.RESET) {
                    ch = matrixChars[Random.nextInt(matrixChars.length)]
                }
                canvas.drawText(ch.toString(), x, y, paint)

                if (y > height && Random.nextFloat() > 0.975f) {
                    drops[i] = 0f
                }
                drops[i] += fontSize * 0.8f
            }
        }

        private fun drawNameReveal(canvas: Canvas) {
            if (width <= 0 || height <= 0) return
            val text = customName.ifBlank { "WallPulse" }
            val totalWidth = paint.measureText(text)
            val startX = (width - totalWidth) / 2f
            val y = height / 2f

            paint.textAlign = Paint.Align.LEFT
            paint.textSize = (fontSize * 2.5f).coerceAtLeast(12f)
            paint.typeface = getFont(fontFamily)

            var cx = startX
            for (i in text.indices) {
                val c = text[i]
                val isSettled = phase == Phase.HOLD || i < currentCharIndex

                if (isSettled) {
                    paint.color = revealColor
                    paint.alpha = settledAlpha
                    paint.style = Paint.Style.FILL
                    paint.setShadowLayer(12f, 0f, 0f, revealColor)
                    canvas.drawText(c.toString(), cx, y, paint)
                    paint.clearShadowLayer()
                    paint.strokeWidth = 1.5f
                    paint.style = Paint.Style.STROKE
                    canvas.drawText(c.toString(), cx, y, paint)
                    paint.style = Paint.Style.FILL
                }
                else if (i == currentCharIndex && phase == Phase.REVEAL) {
                    val revealElapsed = System.currentTimeMillis() - charRevealStart
                    val charElapsed = revealElapsed - i * (charDecoyDuration + 80)
                    val cycle = (charElapsed % charDecoyDuration).toInt()
                    val progress = cycle.toFloat() / charDecoyDuration

                    paint.color = revealColor
                    paint.alpha = ((progress * 255).toInt()).coerceIn(80, 255)
                    val randomChar = matrixChars[Random.nextInt(matrixChars.length)]
                    paint.style = Paint.Style.FILL
                    canvas.drawText(randomChar.toString(), cx, y, paint)
                }

                cx += paint.measureText(c.toString())
            }
        }

        private fun getFont(family: String): Typeface {
            return when (family) {
                "monospace" -> Typeface.MONOSPACE
                "sans_serif" -> Typeface.SANS_SERIF
                "serif" -> Typeface.SERIF
                else -> {
                    try { Typeface.create(family, Typeface.NORMAL) }
                    catch (e: Exception) { Typeface.MONOSPACE }
                }
            }
        }
    }
}
