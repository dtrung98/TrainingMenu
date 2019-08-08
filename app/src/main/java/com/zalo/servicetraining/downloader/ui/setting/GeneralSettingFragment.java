package com.zalo.servicetraining.downloader.ui.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.zalo.servicetraining.R;

public class GeneralSettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.general_setting, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initSummary(getPreferenceScreen());
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(findPreference(key));
    }
    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updateSummary(p);
        }
    }

    private void updateSummary(@Nullable Preference preference) {
        if(preference==null) return;

        if(preference instanceof ListPreference && preference.getKey().equals("connectionsPerTask")) {
            preference.setSummary(((ListPreference) preference).getValue()+" "+getString(R.string.connections));
        } else if(preference instanceof ListPreference && "simultaneousDownloads".equals(preference.getKey())) {
            preference.setSummary(((ListPreference) preference).getValue()+" "+getString(R.string.downloads_same_time));
        } else if(preference instanceof ListPreference && "app_language".equals(preference.getKey())) {
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }
}
