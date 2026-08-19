package com.govindtank.unlockcount

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.BatteryManager
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import android.view.SurfaceHolder
import android.view.animation.PathInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager

private const val ANIMATION_DELAY = 1000L
private const val FRAME_RATE: Long = 60

class UnlockCounterWallpaper : WallpaperService() {

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val size: Float,
        val color: Int
    )

    override fun onCreateEngine(): Engine = UnlockClockWallpaperEngine(this)

    private inner class UnlockClockWallpaperEngine(val context: Context) : Engine() {

        private val prefListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == PreferenceKeys.COUNT_PREFERENCE) handler.postDelayed({
                    updateCounter(
                        sharedPreferences.getInt(PreferenceKeys.COUNT_PREFERENCE, PreferenceKeys.COUNT_PREFERENCE_DEFAULT_VALUE),
                        sharedPreferences.getInt(PreferenceKeys.PREV_COUNT_PREFERENCE, PreferenceKeys.PREV_COUNT_PREFERENCE_DEFAULT_VALUE)
                    )
                }, ANIMATION_DELAY)
            }

        private val handler = Handler()
        private var charHeight: Float = 0f
        private var charWidth: Float = 0f
        private var bottomMargin = 0f
        private var width: Int = 0
        private var height: Int = 0
        private var topMargin = 0f
        private var backgroundColor = Color.BLACK
        private var counterColor = Color.WHITE
        private var count = 0
        private var previousCount = 0
        private val drawRunner = Runnable { draw() }

        private val interpolator = PathInterpolator(
            Path().apply { cubicTo(0.94f, 0.0f, 0.5f, 1f, 1f, 1f) }
        )

        private val letterboxPaint = Paint().apply { color = backgroundColor }

        private val counterTextPaint = TextPaint().apply {
            textAlign = Paint.Align.CENTER
            color = counterColor
            typeface = ResourcesCompat.getFont(context, R.font.sixcaps)
            isAntiAlias = true
        }

        private val unlocksTodayTextPaint = TextPaint().apply {
            textAlign = Paint.Align.CENTER
            color = counterColor
            typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)
            letterSpacing = 0.05f
            isAntiAlias = true
        }

        private val particlePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        private val particles = mutableListOf<Particle>()

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)

            if (isPreview.not()) {
                runCatching {
                    context.registerReceiver(UnlockBroadcastReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
                }
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@UnlockCounterWallpaper)
                prefs.registerOnSharedPreferenceChangeListener(prefListener)
                count = prefs.getInt(PreferenceKeys.COUNT_PREFERENCE, PreferenceKeys.COUNT_PREFERENCE_DEFAULT_VALUE)
                applyCustomization(prefs)
            } else {
                count = PreferenceKeys.PREVIEW_COUNT
                applyCustomization(PreferenceManager.getDefaultSharedPreferences(this@UnlockCounterWallpaper))
            }
        }

        private fun applyCustomization(prefs: SharedPreferences) {
            val darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
            val bgString = prefs.getString(PreferenceKeys.KEY_BACKGROUND_COLOR, null)
            val counterString = prefs.getString(PreferenceKeys.KEY_COUNTER_COLOR, null)

            backgroundColor = if (!bgString.isNullOrEmpty()) {
                runCatching { Color.parseColor(bgString) }.getOrDefault(if (darkMode) Color.BLACK else Color.WHITE)
            } else if (darkMode) Color.BLACK else Color.WHITE

            counterColor = if (!counterString.isNullOrEmpty()) {
                runCatching { Color.parseColor(counterString) }.getOrDefault(if (darkMode) Color.WHITE else Color.BLACK)
            } else if (darkMode) Color.WHITE else Color.BLACK

            letterboxPaint.color = backgroundColor
            counterTextPaint.color = counterColor
            unlocksTodayTextPaint.color = counterColor
        }

        private fun calculateCharWidth(): Float {
            val widths = FloatArray(1)
            counterTextPaint.getTextWidths("0", 0, 1, widths)
            return widths.first()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height

            counterTextPaint.textSize = dpToPx(context, 400f)
            unlocksTodayTextPaint.textSize = dpToPx(context, 14f)
            this.charHeight = counterTextPaint.textSize
            this.charWidth = calculateCharWidth()
            this.topMargin = (height / 2f) - (charHeight / 1.7f)
            this.bottomMargin = dpToPx(context, 30f) + counterTextPaint.textSize + topMargin
        }

        private var movementPosition = 0f
        private var movementSpeed = 1f / (FRAME_RATE * (22f / 25f))

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas()
            if (canvas == null) {
                if (movementPosition < 1.0) handler.postDelayed(drawRunner, 1000 / FRAME_RATE)
                return
            }
            try {
                canvas.drawColor(backgroundColor)
                if (movementPosition < 1.0) movementPosition = Math.min(1.0f, movementPosition + movementSpeed)

                drawCounter(canvas, count, previousCount)
                drawLetterbox(canvas)
                drawParticles(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }

            if (movementPosition < 1.0) handler.postDelayed(drawRunner, 1000 / FRAME_RATE)
        }

        private fun drawLetterbox(canvas: Canvas) {
            canvas.drawRect(Rect(0, 0, width, topMargin.toInt()), letterboxPaint)
            canvas.drawRect(Rect(0, bottomMargin.toInt(), width, height), letterboxPaint)
        }

        private fun drawCounter(canvas: Canvas, count: Int, previousCount: Int) {
            val prevUnits = previousCount % 10
            val prevTens = (previousCount / 10) % 10
            val prevHundreds = (previousCount / 100) % 100

            val units = count % 10
            val tens = (count / 10) % 10
            val hundreds = (count / 100) % 100

            when {
                hundreds > 0 -> {
                    drawNumber(canvas, hundreds, prevHundreds, -charWidth)
                    drawNumber(canvas, tens, prevTens)
                    drawNumber(canvas, units, prevUnits, +charWidth)
                }
                tens > 0 -> {
                    drawNumber(canvas, tens, prevTens, -charWidth * 0.5f)
                    drawNumber(canvas, units, prevUnits, +charWidth * 0.5f)
                }
                else -> {
                    drawNumber(canvas, units, prevUnits)
                }
            }

            when {
                prevHundreds > 0 -> {
                    drawNumber(canvas, prevHundreds, hundreds, -charWidth, counterTextPaint.textSize)
                    drawNumber(canvas, prevTens, tens, 0f, counterTextPaint.textSize)
                    drawNumber(canvas, prevUnits, units, +charWidth, counterTextPaint.textSize)
                }
                prevTens > 0 -> {
                    drawNumber(canvas, prevTens, tens, -charWidth * 0.5f, counterTextPaint.textSize)
                    drawNumber(canvas, prevUnits, units, +charWidth * 0.5f, counterTextPaint.textSize)
                }
                else -> {
                    drawNumber(canvas, prevUnits, units, 0f, counterTextPaint.textSize)
                }
            }
        }

        private fun drawNumber(canvas: Canvas, number: Int, comparisonNumber: Int, xOffset: Float = 0f, rowOffset: Float = 0f) {
            val positionMultiplier = if (number != comparisonNumber) movementPosition else 0f
            val yPosition = rowOffset + topMargin + charHeight * interpolator.getInterpolation(positionMultiplier)
            if (yPosition <= topMargin || yPosition >= bottomMargin + (charHeight * 0.85f)) return
            canvas.drawText(number.toString(), width / 2f + xOffset, yPosition, counterTextPaint)
        }

        private fun updateCounter(counter: Int, previous: Int) {
            count = counter
            previousCount = previous
            movementPosition = 0f
            spawnUnlockParticles()
            draw()
        }

        private fun spawnUnlockParticles() {
            val centerX = width / 2f
            val centerY = topMargin + charHeight / 2f
            val density = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PreferenceKeys.KEY_PARTICLE_DENSITY, 40)
            repeat(density.coerceIn(0, 120)) {
                particles.add(
                    Particle(
                        x = centerX,
                        y = centerY,
                        vx = (Math.random().toFloat() - 0.5f) * 6f,
                        vy = (Math.random().toFloat() - 0.5f) * 6f,
                        life = 1f,
                        size = (Math.random() * 4 + 2).toFloat(),
                        color = counterColor
                    )
                )
            }
        }

        private fun drawParticles(canvas: Canvas) {
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.x += p.vx
                p.y += p.vy
                p.life -= 0.02f
                if (p.life <= 0f) {
                    iterator.remove()
                    continue
                }
                particlePaint.color = p.color
                particlePaint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(p.x, p.y, p.size, particlePaint)
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
