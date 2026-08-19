package com.govindtank.unlockcount.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TimeFlowSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, TimeFlowPreferenceFragment())
                .commit()
        }
    }
}
