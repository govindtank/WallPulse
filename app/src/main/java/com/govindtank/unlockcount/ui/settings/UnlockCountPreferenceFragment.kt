package com.govindtank.unlockcount.ui.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.govindtank.unlockcount.PreferenceKeys
import com.govindtank.unlockcount.R

class UnlockCountPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)

        val bgPref = findPreference<EditTextPreference>(PreferenceKeys.KEY_BACKGROUND_COLOR)
        val counterPref = findPreference<EditTextPreference>(PreferenceKeys.KEY_COUNTER_COLOR)
        val speedPref = findPreference<EditTextPreference>(PreferenceKeys.KEY_ANIMATION_SPEED)
        val particlePref = findPreference<EditTextPreference>(PreferenceKeys.KEY_PARTICLE_DENSITY)
        val darkModePref = findPreference<SwitchPreferenceCompat>(PreferenceKeys.KEY_DARK_MODE)

        bgPref?.setOnPreferenceChangeListener { _, newValue ->
            val text = newValue as String
            text.startsWith("#") && text.length in setOf(7, 9)
        }

        counterPref?.setOnPreferenceChangeListener { _, newValue ->
            val text = newValue as String
            text.startsWith("#") && text.length in setOf(7, 9)
        }

        speedPref?.setOnPreferenceChangeListener { _, newValue ->
            val text = newValue as String
            text.toFloatOrNull()?.let { it > 0f } ?: false
        }

        particlePref?.setOnPreferenceChangeListener { _, newValue ->
            val text = newValue as String
            text.toIntOrNull()?.let { it >= 0 } ?: false
        }
    }
}
