package com.zalo.trainingmenu.model;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

public class Item {
    private final String mTitle;
    private final String mDescription;
    private final Class<? extends AppCompatActivity> mActivityCls;
    private final int mDrawablePadding;

    @DrawableRes
    private Integer mDrawableRes;

    public static class Builder {
        public Context mContext;
        private String mTitle = "";
        private String mDescription = "";
        private Class<? extends AppCompatActivity> mActivityCls;

        public Builder setDrawableRes(Integer drawableRes) {
            mDrawableRes = drawableRes;
            return this;
        }

        private Integer mDrawableRes ;
        private int mDrawablePadding = 0;

        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setTitle(@StringRes int res) {
            return setTitle(mContext.getResources().getString(res));
        }

        public Builder setDescription(String description) {
            mDescription = description;
            return this;
        }

        public Builder setDescription(@StringRes Integer res) {
            return setDescription(mContext.getResources().getString(res));
        }

        public Builder setActivityCls(Class<? extends AppCompatActivity> activityCls) {
            mActivityCls = activityCls;
            return this;
        }

        public Builder setDrawablePadding(int drawablePadding) {
            mDrawablePadding = drawablePadding;
            return this;
        }

        private Builder(Context context) {
            mContext = context;
        }

        public Builder setDestinationActivityClass(Class<? extends AppCompatActivity> cls) {
            mActivityCls = cls;
            return this;
        }

        public Item get(){
            mContext = null;
            return new Item(mTitle,mDescription,mActivityCls,mDrawableRes, mDrawablePadding);
        }
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    private Item(String mTitle, String mDescription, Class<? extends AppCompatActivity> cls, Integer drawableRes, int drawablePadding ) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        mActivityCls = cls;
        mDrawableRes = drawableRes;
        mDrawablePadding = drawablePadding;
    }

    public String getTitle() {
        return mTitle;
    }

    @DrawableRes
    public Integer getDrawableRes() {
        return mDrawableRes;
    }

    public String getDescription() {
        return mDescription;
    }

    public Class<? extends AppCompatActivity> getDestinationActivityClass() {
        return mActivityCls;
    }

    public int getDrawablePadding() {
        return mDrawablePadding;
    }
}
