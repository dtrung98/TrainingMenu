package com.zalo.servicetraining.ui.base;

public abstract class TitleRunnable implements Runnable {
    private final String mTitle;
    private final String mDescription;

    public TitleRunnable(String title, String description) {
        mTitle = title;
        mDescription = description;
    }
}
