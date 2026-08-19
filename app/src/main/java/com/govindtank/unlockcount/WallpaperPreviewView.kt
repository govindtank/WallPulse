package com.govindtank.unlockcount

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class WallpaperPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var modeKey: String = ModeKeys.MODE_CLASSIC
    private var time = 0f
    private val paint = Paint().apply { isAntiAlias = true }
    private val particles = mutableListOf<Particle>()
    private val handler = android.os.Handler()
    private val drawRunner = Runnable { drawFrame() }

    init {
        for (i in 0 until 40) {
            particles.add(
                Particle(
                    x = (i / 40f),
                    y = 0.5f,
                    baseY = 0.5f,
                    amplitude = 0.1f + Math.random().toFloat() * 0.15f,
                    frequency = 0.02f + Math.random().toFloat() * 0.03f,
                    phase = Math.random().toFloat() * (Math.PI * 2).toFloat()
                )
            )
        }
    }

    fun setMode(mode: String) {
        modeKey = mode
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(drawRunner)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(drawRunner)
    }

    private fun drawFrame() {
        time += 0.02f
        invalidate()
        handler.postDelayed(drawRunner, 50)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        when (modeKey) {
            ModeKeys.MODE_CLASSIC -> drawClassicPreview(canvas, w, h)
            ModeKeys.MODE_TIME_FLOW -> drawTimeFlowPreview(canvas, w, h)
            ModeKeys.MODE_PARTICLE_WAVE -> drawParticleWavePreview(canvas, w, h)
            ModeKeys.MODE_GRADIENT_PULSE -> drawGradientPulsePreview(canvas, w, h)
            ModeKeys.MODE_DATA_STREAM -> drawDataStreamPreview(canvas, w, h)
            ModeKeys.MODE_AURORA -> drawAuroraPreview(canvas, w, h)
            ModeKeys.MODE_MATRIX_RAIN -> drawMatrixRainPreview(canvas, w, h)
            else -> drawClassicPreview(canvas, w, h)
        }
    }

    private fun drawClassicPreview(canvas: Canvas, w: Float, h: Float) {
        canvas.drawColor(Color.BLACK)
        paint.color = Color.WHITE
        paint.textSize = h * 0.6f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("7", w / 2, h / 2 + h * 0.15f, paint)
        paint.textSize = h * 0.15f
        canvas.drawText("unlocks", w / 2, h / 2 - h * 0.1f, paint)
    }

    private fun drawTimeFlowPreview(canvas: Canvas, w: Float, h: Float) {
        canvas.drawColor(Color.BLACK)
        val cx = w / 2
        val cy = h / 2
        val radius = minOf(w, h) / 2.5f
        paint.color = Color.CYAN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, 180f, 180f, false, paint)
        paint.style = Paint.Style.FILL
        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 6 / 24f * 360 - 90).toDouble())
            val x = (cx + radius * cos(angle)).toFloat()
            val y = (cy + radius * sin(angle)).toFloat()
            canvas.drawCircle(x, y, 3f, paint)
        }
    }

    private fun drawParticleWavePreview(canvas: Canvas, w: Float, h: Float) {
        canvas.drawColor(Color.BLACK)
        paint.color = Color.GREEN
        paint.style = Paint.Style.FILL
        for (particle in particles) {
            val y = h / 2 + sin(time * 2 + particle.phase + particle.x * particle.frequency * 100f) * h * particle.amplitude
            canvas.drawCircle(particle.x * w, y, 2f, paint)
        }
    }

    private fun drawGradientPulsePreview(canvas: Canvas, w: Float, h: Float) {
        val gradient = LinearGradient(0f, 0f, w, h, Color.MAGENTA, Color.BLUE, Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        val pulse = (sin(time * 2) * 0.5f + 0.5f) * minOf(w, h) / 2
        paint.color = Color.WHITE
        paint.alpha = 80
        canvas.drawCircle(w / 2, h / 2, pulse, paint)
    }

    private fun drawDataStreamPreview(canvas: Canvas, w: Float, h: Float) {
        canvas.drawColor(Color.BLACK)
        paint.color = Color.GREEN
        paint.alpha = 180
        paint.textSize = 10f
        for (i in 0 until 8) {
            val x = (i / 8f) * w
            val y = ((time * 50 + i * 30) % h)
            canvas.drawText(((Math.random() * 2)).toInt().toString(), x, y, paint)
        }
    }

    private fun drawAuroraPreview(canvas: Canvas, w: Float, h: Float) {
        canvas.drawColor(Color.BLACK)
        for (i in 0 until 3) {
            val phase = time + i * 0.5f
            val yOffset = h / 2 + sin(phase) * h * 0.2f
            val gradient = LinearGradient(0f, yOffset - 20, w, yOffset + 20, Color.CYAN, Color.MAGENTA, Shader.TileMode.MIRROR)
            paint.shader = gradient
            paint.alpha = 40
            canvas.drawRect(0f, yOffset - 20, w, yOffset + 20, paint)
        }
        paint.shader = null
    }

    private fun drawMatrixRainPreview(canvas: Canvas, w: Float, h: Float) {
        canvas.drawColor(Color.BLACK)
        paint.color = Color.GREEN
        paint.alpha = 180
        paint.textSize = 10f
        for (i in 0 until 10) {
            val x = (i / 10f) * w
            val y = ((time * 30 + i * 20) % h)
            canvas.drawText(((Math.random() * 94 + 33).toInt().toChar()).toString(), x, y, paint)
        }
    }
}
