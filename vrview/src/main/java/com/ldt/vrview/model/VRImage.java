package com.ldt.vrview.model;

import android.content.Context;
import android.graphics.Bitmap;

public class VRImage {
    private static final String TAG = "VRImageObject";

    private final float[] mStartingVector;
    private float[] mCurrentVector = new float[3];

    private float mCurrentScale;
    private final float mStartingScale;
    private final Bitmap mThumbnailBitmap;
    private final Bitmap mBitmap;

    public VRImage(Builder builder) {
        mThumbnailBitmap = builder.mThumbnailBitmap;
        mBitmap = builder.mBitmap;
        mCurrentVector = mStartingVector = builder.mStartingVector;
        mCurrentScale = mStartingScale = builder.mStartingScale;
    }

    public float getCurrentScale() {
        return mCurrentScale;
    }

    public void setCurrentScale(float currentScale) {
        mCurrentScale = currentScale;
    }

    public float getStartingScale() {
        return mStartingScale;
    }


    public VRImage setStartingVector(float x, float y, float z) {
        mStartingVector[0] = x;
        mStartingVector[1] = y;
        mStartingVector[2] = z;
        return this;
    }

    public VRImage setCurrentVector(float x, float y, float z) {
        mCurrentVector[0] = x;
        mCurrentVector[1] = y;
        mCurrentVector[2] = z;
        return this;
    }


    public float[] getStartingVector() {
        float[] temp = new float[3];
        System.arraycopy(mStartingVector,0,temp,0,3);
        return temp;
    }

    public float[] getCurrentVector() {
        float[] temp = new float[3];
        System.arraycopy(mCurrentVector,0,temp,0,3);
        return temp;
    }


    public Bitmap getThumbnailBitmap() {
        return mThumbnailBitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Builder with(Context context) {
        return new Builder(context);
    }

    private static class Builder {
        private Context mContext;
        private Builder(Context context) {
            mContext = context;
        }

        private float[] mStartingVector = new float[3];

        private float mStartingScale = 1;
        private Bitmap mThumbnailBitmap;
        private Bitmap mBitmap;
        public Builder setStartingScale(float value) {
            mStartingScale = value;
            return this;
        }

        public Builder setStartingVector(float[] vector3) {
            System.arraycopy(vector3,0,mStartingVector,0,3);
            return this;
        }

        public Builder setThumbNail(Bitmap bitmap) {
            mThumbnailBitmap = bitmap;
            return this;
        }

        public Builder setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
            return this;
        }

        public VRImage build() {
            mContext = null;
            return new VRImage(this);
        }
    }
}
