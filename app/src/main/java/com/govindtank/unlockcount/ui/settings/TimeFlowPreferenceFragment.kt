package com.govindtank.unlockcount.ui.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.govindtank.unlockcount.PreferenceKeys
import com.govindtank.unlockcount.R

class TimeFlowPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.time_flow_prefs, rootKey)

        val dotColor = findPreference<EditTextPreference>(PreferenceKeys.TF_KEY_DOT_COLOR)
        val arcColor = findPreference<EditTextPreference>(PreferenceKeys.TF_KEY_ARC_COLOR)
        dotColor?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        arcColor?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
    }
}
