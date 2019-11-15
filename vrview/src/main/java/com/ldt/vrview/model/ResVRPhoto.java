package com.ldt.vrview.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.DrawableRes;

public class ResVRPhoto extends VRPhoto {
    public ResVRPhoto(Builder builder) {
        super(builder);
        mDrawableRes = builder.mDrawableRes;
    }

    @DrawableRes
    private final int mDrawableRes;

    public void buildBitmapFromRes(Context context) {
        try {
            setBitmap(BitmapFactory.decodeResource(context.getResources(),mDrawableRes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bitmap getBitmap() {
        return super.getBitmap();
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    private static class Builder extends VRPhoto.Builder {
        private int mDrawableRes = -1;
        public Builder setDrawableRes(@DrawableRes int id) {
            mDrawableRes = id;
            return this;
        }

        public Builder(Context context) {
            super(context);
        }
    }
}
