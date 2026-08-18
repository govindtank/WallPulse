package com.govindtank.unlockcount

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View

class SetWallpaperActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        val intent = Intent(
            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        )
        Log.d("click","before")
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            //unlock conter activity commented
//            ComponentName(this, UnlockCounterWallpaper::class.java)
            ComponentName(this, MyWallpaperService::class.java)

        )
        startActivity(intent)
        Log.d("click","after changing live wallpaper")
    }
}
