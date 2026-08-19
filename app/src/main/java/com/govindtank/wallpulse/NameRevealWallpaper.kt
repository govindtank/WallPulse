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
        private var rainDuration = 4000L
        private var holdDuration = 30000L
        private var columnSpacing = 28f
        private var charSpacing = 12f
        private var decoySpeed = 50L
        private var startTime = 0L
        private var phase = Phase.RAIN
        private var currentCharIndex = 0
        private var charRevealStart = 0L
        private var paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val matrixChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#$%&*ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ"
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
                rainDuration = prefs.getInt(PreferenceKeys.NAME_REVEAL_DURATION, 4000).coerceIn(1000, 30000).toLong()
                holdDuration = prefs.getInt(PreferenceKeys.NAME_REVEAL_HOLD_DURATION, 30000).coerceIn(5000, 120000).toLong()
                columnSpacing = prefs.getInt(PreferenceKeys.NAME_REVEAL_COLUMN_SPACING, 28).coerceIn(12, 120).toFloat()
                charSpacing = prefs.getInt(PreferenceKeys.NAME_REVEAL_CHAR_SPACING, 12).coerceIn(4, 60).toFloat()
                decoySpeed = prefs.getInt(PreferenceKeys.NAME_REVEAL_DECOY_SPEED, 50).coerceIn(20, 300).toLong()
                darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
            } catch (e: Exception) {
                resetDefaults()
            }
        }

        private fun resetDefaults() {
            customName = "Govind"
            matrixColor = Color.GREEN
            revealColor = Color.WHITE
            fontSize = 24f
            fontFamily = "monospace"
            rainDuration = 4000L
            holdDuration = 30000L
            columnSpacing = 28f
            charSpacing = 12f
            decoySpeed = 50L
            darkMode = true
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                if (darkMode) canvas.drawColor(Color.BLACK) else canvas.drawColor(Color.WHITE)
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
                    if (elapsed >= rainDuration) {
                        phase = Phase.REVEAL
                        currentCharIndex = 0
                        charRevealStart = now
                    }
                }
                Phase.REVEAL -> {
                    val revealElapsed = now - charRevealStart
                    val charsToReveal = (revealElapsed / (decoySpeed + 60)).toInt()
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
                    if (now - startTime >= 1200) {
                        phase = Phase.RAIN
                        currentCharIndex = 0
                        startTime = now
                    }
                }
            }
        }

        private fun drawMatrixRain(canvas: Canvas) {
            if (width <= 0 || height <= 0) return
            val textSize = fontSize.coerceAtLeast(10f)
            paint.textSize = textSize
            paint.typeface = getFont(fontFamily)

            for (i in 0 until columns) {
                val x = i * columnSpacing
                val y = drops[i]
                val ch = matrixChars[Random.nextInt(matrixChars.length)].toString()

                // Bright head
                paint.color = matrixColor
                paint.alpha = 255
                canvas.drawText(ch, x, y, paint)

                // Trail
                paint.color = matrixColor
                paint.alpha = 100
                canvas.drawText(ch, x, y - textSize, paint)
                paint.alpha = 50
                canvas.drawText(ch, x, y - textSize * 2, paint)
                paint.alpha = 20
                canvas.drawText(ch, x, y - textSize * 3, paint)

                if (y > height && Random.nextFloat() > 0.975f) {
                    drops[i] = 0f
                }
                drops[i] += textSize * 0.9f
            }
        }

        private fun drawNameReveal(canvas: Canvas) {
            if (width <= 0 || height <= 0) return
            val text = customName.ifBlank { "WallPulse" }
            val textSize = (fontSize * 2.2f).coerceAtLeast(14f)
            paint.textSize = textSize
            paint.typeface = getFont(fontFamily)
            paint.textAlign = Paint.Align.LEFT

            val totalWidth = text.length * (textSize * 0.72f + charSpacing)
            val startX = (width - totalWidth) / 2f
            val y = height / 2f + textSize * 0.35f

            var cx = startX
            for (i in text.indices) {
                val c = text[i]
                val isSettled = phase == Phase.HOLD || i < currentCharIndex

                if (isSettled) {
                    // Glow
                    paint.color = revealColor
                    paint.alpha = 255
                    paint.style = Paint.Style.FILL
                    paint.setShadowLayer(16f, 0f, 0f, revealColor)
                    canvas.drawText(c.toString(), cx, y, paint)
                    paint.clearShadowLayer()

                    // Stroke
                    paint.strokeWidth = 1.5f
                    paint.style = Paint.Style.STROKE
                    paint.color = if (darkMode) Color.BLACK else Color.WHITE
                    canvas.drawText(c.toString(), cx, y, paint)

                    // Fill
                    paint.style = Paint.Style.FILL
                    paint.color = revealColor
                    canvas.drawText(c.toString(), cx, y, paint)
                } else if (i == currentCharIndex && phase == Phase.REVEAL) {
                    val revealElapsed = System.currentTimeMillis() - charRevealStart
                    val charElapsed = revealElapsed - i * (decoySpeed + 60)
                    if (charElapsed < 0) {
                        cx += textSize * 0.72f + charSpacing
                        continue
                    }
                    val cycle = (charElapsed % decoySpeed).toInt()
                    val progress = cycle.toFloat() / decoySpeed

                    paint.color = revealColor
                    paint.alpha = ((progress * 255).toInt()).coerceIn(100, 255)
                    val randomChar = matrixChars[Random.nextInt(matrixChars.length)].toString()
                    paint.style = Paint.Style.FILL
                    paint.setShadowLayer(10f, 0f, 0f, revealColor)
                    canvas.drawText(randomChar, cx, y, paint)
                    paint.clearShadowLayer()
                }

                cx += textSize * 0.72f + charSpacing
            }
        }

        private fun getFont(family: String): Typeface {
            return when (family) {
                "monospace" -> Typeface.MONOSPACE
                "sans_serif" -> Typeface.SANS_SERIF
                "serif" -> Typeface.SERIF
                else -> try { Typeface.create(family, Typeface.NORMAL) } catch (e: Exception) { Typeface.MONOSPACE }
            }
        }
    }
}
