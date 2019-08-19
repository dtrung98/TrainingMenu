package com.zalo.trainingmenu.downloader.ui.setting;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

public class FolderChooserPreference extends DialogPreference {
    public FolderChooserPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FolderChooserPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FolderChooserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderChooserPreference(Context context) {
        super(context);
    }
}
