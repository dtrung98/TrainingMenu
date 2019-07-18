package com.zalo.servicetraining.model;

import androidx.appcompat.app.AppCompatActivity;

public class Item {
    private String mTitle = "";
    private String mDescription = "";
    private Class<? extends AppCompatActivity> mActivityCls;

    public Item() {
    }

    public Item(String mTitle, String mDescription, Class<? extends AppCompatActivity> cls) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        mActivityCls = cls;
    }

    public String getTitle() {
        return mTitle;
    }

    public Item setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Item setDescription(String mDescription) {
        this.mDescription = mDescription;
        return this;
    }

    public Class<? extends AppCompatActivity> getDestinationActivityClass() {
        return mActivityCls;
    }

    public Item setDestinationActivityClass(Class<? extends  AppCompatActivity> cls) {
        mActivityCls = cls;
        return this;
    }
}
