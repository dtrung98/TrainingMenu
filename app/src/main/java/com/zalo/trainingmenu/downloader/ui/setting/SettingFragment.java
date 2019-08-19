package com.zalo.trainingmenu.downloader.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.helper.LocaleHelper;
import com.zalo.trainingmenu.downloader.ui.base.PermissionActivity;
import com.zalo.trainingmenu.util.Util;

import java.io.File;

import static com.zalo.trainingmenu.downloader.ui.setting.SettingActivity.ACTION_CHOOSE_DOWNLOAD_FOLDER;

public class SettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, FolderChooserDialog.FolderCallback {
    private static final String TAG = "SettingFragment";

    public static final String EXTRA_CONNECTIONS_PER_TASK = "connectionsPerTask";
    public static final String EXTRA_SIMULTANEOUS_DOWNLOADS = "simultaneousDownloads";
    public static final String EXTRA_APP_LANGUAGE = "app_language";
    public static final String DOWNLOADS_FOLDER = "downloadsFolder";

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
        if (preference == null) return;
        if(preference instanceof FolderChooserPreference && DOWNLOADS_FOLDER.equals(preference.getKey())) {
          preference.setSummary(Util.getCurrentDownloadDirectoryPath());
        } else if (preference instanceof ListPreference && EXTRA_CONNECTIONS_PER_TASK.equals(preference.getKey())) {
            preference.setSummary(((ListPreference) preference).getValue() + " " + getString(R.string.connections));
        } else if (preference instanceof ListPreference && EXTRA_SIMULTANEOUS_DOWNLOADS.equals(preference.getKey())) {
            preference.setSummary(((ListPreference) preference).getValue() + " " + getString(R.string.downloads_same_time));
        } else if (preference instanceof ListPreference && EXTRA_APP_LANGUAGE.equals(preference.getKey())) {

            CharSequence entry = ((ListPreference) preference).getEntry();
            String value = ((ListPreference) preference).getValue();
            String currentLang = LocaleHelper.getLanguage(App.getInstance().getApplicationContext());

            if(entry==null||value==null) {
                value = currentLang;
                entry = value.equals("en") ? "English" : "Tiếng Việt";
            }

            if (!value.equals(currentLang)) {
                LocaleHelper.setLocale(App.getInstance().getApplicationContext(),value);
                //LocaleHelper.setLocale(getActivity(),((ListPreference) preference).getValue());
                if(getActivity() !=null)
                getActivity().recreate();
            }
            preference.setSummary(entry);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if(preference instanceof FolderChooserPreference) {
            if(getActivity() instanceof PermissionActivity) {
                Intent intent = new Intent(ACTION_CHOOSE_DOWNLOAD_FOLDER);
                ((PermissionActivity) getActivity()).executeWriteStorageAction(intent);
            }
        }
        else
        super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onFolderSelection(@NonNull File folder) {
        SharedPreferences.Editor editor = App.getDefaultSharedPreferences().edit();
        editor.putString("downloadsFolder", folder.getAbsolutePath());
        editor.apply();
    }
}
