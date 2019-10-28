package com.ldt.vrview.model;

import android.graphics.Bitmap;

public class VRImageObject {
    private static final String TAG = "VRImageObject";

    private float[] mStartingVector = new float[3];
    private float[] mCurrentVector = new float[3];

    private float mCurrentScale = 1;
    private float mStartingScale = 1;
    private Bitmap mThumbnailBitmap;
    private Bitmap mBitmap;

    public float getCurrentScale() {
        return mCurrentScale;
    }

    public void setCurrentScale(float currentScale) {
        mCurrentScale = currentScale;
    }

    public float getStartingScale() {
        return mStartingScale;
    }

    public void setStartingScale(float startingScale) {
        mStartingScale = startingScale;
    }

    public VRImageObject setStartingVector(float x, float y, float z) {
        mStartingVector[0] = x;
        mStartingVector[1] = y;
        mStartingVector[2] = z;
        return this;
    }

    public VRImageObject setCurrentVector(float x, float y, float z) {
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

    public void setStartingVector(float[] startingVector) {
        mStartingVector = startingVector;
    }

    public Bitmap getThumbnailBitmap() {
        return mThumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        mThumbnailBitmap = thumbnailBitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public VRImageObject setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }
}
