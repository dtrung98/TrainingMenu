package com.zalo.servicetraining.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.zalo.servicetraining.App;

public class Item {
    private String mTitle = "";
    private String mDescription = "";
    private Class<? extends AppCompatActivity> mActivityCls;
    private int mDrawablePadding = 0;

    @DrawableRes
    private Integer mDrawableRes;

    public Item() {
    }

    public Item(String mTitle, String mDescription, Class<? extends AppCompatActivity> cls) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        mActivityCls = cls;
    }

    public Item(@StringRes int titleRes, @StringRes int descriptionRes, Class<? extends AppCompatActivity> cls) {
        setTitle(titleRes).setDescription(descriptionRes).setDestinationActivityClass(cls);
    }

    public String getTitle() {
        return mTitle;
    }

    public Item setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public Item setTitle(@StringRes int res) {
        return setTitle(App.getInstance().getApplicationContext().getResources().getString(res));
    }
    public Item setDrawable(@DrawableRes int rest) {
        mDrawableRes = rest;
        return this;
    }

    @DrawableRes
    public Integer getDrawableRes() {
        return mDrawableRes;
    }

    public String getDescription() {
        return mDescription;
    }

    public Item setDescription(String mDescription) {
        this.mDescription = mDescription;
        return this;
    }

    public Item setDescription(@StringRes int res) {
        return setDescription(App.getInstance().getApplicationContext().getResources().getString(res));
    }

    public Class<? extends AppCompatActivity> getDestinationActivityClass() {
        return mActivityCls;
    }

    public Item setDestinationActivityClass(Class<? extends  AppCompatActivity> cls) {
        mActivityCls = cls;
        return this;
    }

    public int getDrawablePadding() {
        return mDrawablePadding;
    }

    public Item setDrawablePadding(int mPaddingDrawableInDp) {
        this.mDrawablePadding = mPaddingDrawableInDp;
        return this;
    }
}
