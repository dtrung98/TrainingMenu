package com.zalo.servicetraining;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.zalo.servicetraining.downloader.helper.LocaleHelper;

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
}
