package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager

class MatrixRainWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = MatrixRainEngine(this)

    private inner class MatrixRainEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var columns = 0
        private var drops = mutableListOf<Float>()
        private var streamColor = Color.GREEN
        private var darkMode = true
        private var unlockCount = 0
        private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            unlockCount = DataRepository.getUnlockCount(context)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            columns = (width / 30f).toInt().coerceAtLeast(1)
            drops = mutableListOf<Float>().apply {
                for (i in 0 until columns) add(Math.random().toFloat() * height)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun loadPrefs() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            streamColor = prefs.getInt(PreferenceKeys.PREF_MATRIX_COLOR, Color.GREEN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                paint.color = streamColor
                paint.alpha = 180
                paint.textSize = 20f
                for (i in 0 until columns) {
                    val x = i * 30f
                    val y = drops[i]
                    val char = (Math.random() * 94 + 33).toInt().toChar()
                    canvas.drawText(char.toString(), x, y, paint)
                    if (y > height && Math.random() > 0.975) drops[i] = 0f
                    drops[i] += 30f
                }
                drawUnlockCount(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 50)
        }

        private fun drawUnlockCount(canvas: Canvas) {
            val cx = width / 2f
            val cy = height - dpToPx(context, 48f)
            paint.color = if (darkMode) Color.WHITE else Color.BLACK
            paint.textSize = dpToPx(context, 14f)
            canvas.drawText("Unlocks: $unlockCount", cx, cy, paint)
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
