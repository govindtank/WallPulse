package com.govindtank.wallpulse

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import kotlin.math.sin

class NotificationsWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = NotificationsEngine(this)

    private inner class NotificationsEngine(val context: Context) : Engine() {
        private val handler = Handler()
        private var width = 0
        private var height = 0
        private var time = 0f
        private var primaryColor = Color.CYAN
        private var darkMode = true
        private var notifications = 0
        private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val drawRunner = Runnable { draw() }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPrefs()
            notifications = DataRepository.getNotificationCount(context)
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
            primaryColor = prefs.getInt(PreferenceKeys.PREF_NOTIFICATIONS_PRIMARY, Color.CYAN)
            darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, true)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(if (darkMode) Color.BLACK else Color.WHITE)
                time += 0.02f
                drawBubbles(canvas)
                drawCount(canvas)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            handler.postDelayed(drawRunner, 16)
        }

        private fun drawBubbles(canvas: Canvas) {
            val count = minOf(notifications, 12)
            for (i in 0 until count) {
                val x = width * 0.2f + (i % 4) * (width * 0.2f)
                val y = height * 0.3f + (i / 4) * (height * 0.15f)
                val scale = 0.8f + sin(time + i) * 0.2f
                paint.color = primaryColor
                paint.alpha = 180
                canvas.drawCircle(x, y, 18f * scale, paint)
            }
        }

        private fun drawCount(canvas: Canvas) {
            val cx = width / 2f
            val cy = height - dpToPx(context, 48f)
            paint.color = if (darkMode) Color.WHITE else Color.BLACK
            paint.textSize = dpToPx(context, 14f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Notifications: $notifications", cx, cy, paint)
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}
