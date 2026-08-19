package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.sin
import kotlin.math.cos

class CosmicDustWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = CosmicDustEngine(this)

    data class Star(var x: Float, var y: Float, val size: Float, val twinkleSpeed: Float, val phase: Float)

    private inner class CosmicDustEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var stars = mutableListOf<Star>()
        private var nebulaColor = Color.MAGENTA
        private var darkMode = true
        private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            generateStars()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            generateStars()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            nebulaColor = prefs.getInt(PreferenceKeys.PREF_COSMIC_NEBULA, Color.MAGENTA)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun generateStars() {
            stars = mutableListOf()
            val count = 150
            for (i in 0 until count) {
                stars.add(
                    Star(
                        x = Math.random().toFloat() * width,
                        y = Math.random().toFloat() * height,
                        size = (Math.random().toFloat() * 3 + 1).coerceAtMost(4f),
                        twinkleSpeed = (Math.random().toFloat() * 0.05f + 0.02f),
                        phase = Math.random().toFloat() * (Math.PI * 2).toFloat()
                    )
                )
            }
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(Color.BLACK)
                drawNebula(canvas)
                drawStars(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawNebula(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            val radius = Math.max(width, height).toFloat() / 2
            val gradient = RadialGradient(cx, cy, radius, nebulaColor, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            paint.shader = gradient
            paint.alpha = 60
            canvas.drawCircle(cx, cy, radius, paint)
            paint.shader = null
        }

        private fun drawStars(canvas: Canvas) {
            val time = System.currentTimeMillis() / 1000f
            for (star in stars) {
                val twinkle = (sin(time * star.twinkleSpeed + star.phase) * 0.5f + 0.5f).coerceIn(0.3f, 1f)
                paint.color = Color.WHITE
                paint.alpha = (twinkle * 255).toInt()
                canvas.drawCircle(star.x, star.y, star.size * twinkle, paint)
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
