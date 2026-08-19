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

    private inner class NameRevealEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var columns = 0
        private var drops = mutableListOf<Float>()
        private var revealProgress = 0f
        private var isRevealed = false
        private var darkMode = true
        private var customName = "Govind"
        private var matrixColor = Color.GREEN
        private var revealColor = Color.WHITE
        private var fontSize = 24f
        private var fontFamily = "monospace"
        private var revealDuration = 3000L
        private var columnSpacing = 30f
        private var paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            initColumns()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
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
            columns = (width / columnSpacing).toInt().coerceAtLeast(1)
            drops = mutableListOf<Float>().apply {
                for (i in 0 until columns) add(Random.nextFloat() * height)
            }
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            customName = prefs.getString(PreferenceKeys.NAME_REVEAL_NAME, "Govind") ?: "Govind"
            matrixColor = prefs.getInt(PreferenceKeys.NAME_REVEAL_MATRIX_COLOR, Color.GREEN)
            revealColor = prefs.getInt(PreferenceKeys.NAME_REVEAL_TEXT_COLOR, Color.WHITE)
            fontSize = prefs.getInt(PreferenceKeys.NAME_REVEAL_FONT_SIZE, 24).toFloat()
            fontFamily = prefs.getString(PreferenceKeys.NAME_REVEAL_FONT_FAMILY, "monospace") ?: "monospace"
            revealDuration = prefs.getInt(PreferenceKeys.NAME_REVEAL_DURATION, 3000).toLong()
            columnSpacing = prefs.getInt(PreferenceKeys.NAME_REVEAL_COLUMN_SPACING, 30).toFloat()
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                
                if (!isRevealed) {
                    drawMatrixRain(canvas)
                    updateRevealProgress()
                } else {
                    drawRevealedName(canvas)
                }
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 50)
        }

        private fun drawMatrixRain(canvas: Canvas) {
            paint.color = matrixColor
            paint.alpha = 180
            paint.textSize = fontSize
            paint.typeface = getFont(fontFamily)
            
            for (i in 0 until columns) {
                val x = i * columnSpacing
                val y = drops[i]
                val char = (Random.nextInt(33, 127)).toChar()
                canvas.drawText(char.toString(), x, y, paint)
                
                if (y > height && Random.nextFloat() > 0.975f) {
                    drops[i] = 0f
                }
                drops[i] += fontSize * 0.8f
            }
        }

        private fun updateRevealProgress() {
            val startTime = System.currentTimeMillis()
            val elapsed = System.currentTimeMillis() - startTime
            revealProgress = (elapsed / revealDuration.toFloat()).coerceIn(0f, 1f)
            
            if (revealProgress >= 1f) {
                isRevealed = true
            }
        }

        private fun drawRevealedName(canvas: Canvas) {
            val text = customName
            paint.color = revealColor
            paint.textSize = fontSize * 3f
            paint.typeface = getFont(fontFamily)
            paint.textAlign = Paint.Align.CENTER
            
            val x = width / 2f
            val y = height / 2f
            
            // Glow effect
            paint.setShadowLayer(20f, 0f, 0f, revealColor)
            canvas.drawText(text, x, y, paint)
            paint.clearShadowLayer()
            
            // Main text
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE
            canvas.drawText(text, x, y, paint)
            paint.style = Paint.Style.FILL
            canvas.drawText(text, x, y, paint)
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
