package com.govindtank.unlockcount.ui.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.govindtank.unlockcount.R

class UnlockCountPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)

        val circlePreference = findPreference<EditTextPreference>("pref_number_of_circles")
        circlePreference?.setOnPreferenceChangeListener { _, newValue ->
            val text = newValue as String
            text.matches(Regex("\\d*"))
        }
    }
}
