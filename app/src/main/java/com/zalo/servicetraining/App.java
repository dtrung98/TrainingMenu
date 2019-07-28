package com.zalo.servicetraining;

import android.app.Application;

import androidx.annotation.NonNull;

public class App extends Application {
    private static App sInstance;

    @Override public void onCreate() {
        super.onCreate();
        sInstance = this;
        init();
    }

    @NonNull
    public static App getInstance() {
        return sInstance;
    }

    private void init() {}
}
