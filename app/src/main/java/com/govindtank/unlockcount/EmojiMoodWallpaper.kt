package com.govindtank.unlockcount

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager

class EmojiMoodWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = EmojiMoodEngine(this)

    private inner class EmojiMoodEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var primaryColor = Color.YELLOW
        private var darkMode = true
        private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
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
            primaryColor = prefs.getInt(PreferenceKeys.PREF_EMOJI_PRIMARY, Color.YELLOW)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                time += 0.01f
                drawMood(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawMood(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            val radius = minOf(width, height) / 3f
            val gradient = RadialGradient(cx, cy, radius, primaryColor, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            paint.shader = gradient
            canvas.drawCircle(cx, cy, radius, paint)
            paint.shader = null
            paint.color = if (darkMode) Color.WHITE else Color.BLACK
            paint.textSize = dpToPx(context, 48f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(getMoodEmoji(), cx, cy + dpToPx(context, 16f), paint)
        }

        private fun getMoodEmoji(): String {
            val unlocks = DataRepository.getUnlockCount(context)
            return when {
                unlocks > 100 -> "\uD83D\uDE31"
                unlocks > 50 -> "\uD83D\uDE20"
                unlocks > 20 -> "\uD83D\uDE10"
                unlocks > 5 -> "\uD83D\uDE42"
                else -> "\uD83D\uDE0A"
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
