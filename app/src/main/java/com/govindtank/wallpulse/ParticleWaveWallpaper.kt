package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.sin

class ParticleWaveWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = ParticleWaveEngine(this)

    private inner class ParticleWaveEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var particles = mutableListOf<Particle>()
        private var particleColor = Color.CYAN
        private var darkMode = true
        private var notificationCount = 0
        private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            notificationCount = DataRepository.getNotificationCount(context)
            generateParticles()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            generateParticles()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            particleColor = prefs.getInt(PreferenceKeys.PREF_PARTICLE_COLOR, Color.CYAN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun generateParticles() {
            particles = mutableListOf()
            val count = (40 + notificationCount * 3).coerceAtMost(300)
            for (i in 0 until count) {
                val x = (i / count.toFloat()) * width
                particles.add(
                    Particle(
                        x = x,
                        y = height / 2f,
                        baseY = height / 2f,
                        amplitude = 50 + Math.random().toFloat() * 100,
                        frequency = 0.02f + Math.random().toFloat() * 0.03f,
                        phase = Math.random().toFloat() * (Math.PI * 2).toFloat()
                    )
                )
            }
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                time += 0.02f
                drawWave(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawWave(canvas: Canvas) {
            paint.color = particleColor
            for (particle in particles) {
                val y = particle.baseY + sin(time * 2 + particle.phase + particle.x * particle.frequency) * particle.amplitude
                particle.y = y
                paint.alpha = (150 + sin(time + particle.phase) * 105).toInt().coerceIn(0, 255)
                canvas.drawCircle(particle.x, y, dpToPx(context, 4f), paint)
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}

data class Particle(var x: Float, var y: Float, val baseY: Float, val amplitude: Float, val frequency: Float, val phase: Float)
