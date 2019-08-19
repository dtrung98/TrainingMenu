package com.zalo.trainingmenu;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.zalo.trainingmenu.downloader.helper.LocaleHelper;

public class App extends Application {
    private static App sInstance;

    @Override public void onCreate() {
        super.onCreate();
        sInstance = this;
        init();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @NonNull
    public static App getInstance() {
        return sInstance;
    }

    private void init() {}

    public static SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext());
    }
}
