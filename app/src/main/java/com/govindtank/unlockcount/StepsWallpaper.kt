package com.govindtank.unlockcount

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager

class StepsWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = StepsEngine(this)

    private inner class StepsEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var primaryColor = Color.CYAN
        private var darkMode = true
        private var steps = 0
        private val paint = Paint().apply { isAntiAlias = true }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            steps = DataRepository.getStepCount(context)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            primaryColor = prefs.getInt(PreferenceKeys.PREF_STEPS_PRIMARY, Color.CYAN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                time += 0.01f
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                drawRing(canvas)
                drawSteps(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawRing(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f - dpToPx(context, 24f)
            val radius = minOf(width, height) / 3f
            val progress = (steps / 10000f).coerceIn(0f, 1f)
            val sweep = progress * 360f

            paint.color = if (darkMode) Color.DKGRAY else Color.LTGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dpToPx(context, 12f)
            canvas.drawCircle(cx, cy, radius, paint)

            val gradient = SweepGradient(cx, cy, intArrayOf(primaryColor, primaryColor), floatArrayOf(0f, sweep / 360f))
            paint.shader = gradient
            paint.strokeCap = Paint.Cap.ROUND
            canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, -90f, sweep, false, paint)
            paint.shader = null
        }

        private fun drawSteps(canvas: Canvas) {
            val cx = width / 2f
            val cy = height - dpToPx(context, 48f)
            paint.color = if (darkMode) Color.WHITE else Color.BLACK
            paint.textSize = dpToPx(context, 14f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Steps: $steps", cx, cy, paint)
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
