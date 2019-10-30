package com.ldt.vrview.rotate;

public class BaseRotation implements AbsRotation {
    private float[] mValues = new float[] {0,0,0};
    private float mScale = 1;

    private boolean[] mEnableValues = new boolean[] {true,true,true,true};

    @Override
    public void setValue(int type, float value) {
        mValues[type] = value;
    }

    @Override
    public synchronized void scrollBy(float x, float y, float z) {
        mValues[0] = x;
        mValues[1] = y;
        mValues[2] = z;
    }

    @Override
    public synchronized void scrollBy(float[] values) {
        System.arraycopy(values,0,mValues,0,3);
    }

    @Override
    public synchronized void scrollXBy(float x) {
        mValues[0] = x;
    }

    @Override
    public synchronized void scrollYBy(float y) {
        mValues[1] = y;
    }

    @Override
    public synchronized void scrollZBy(float z) {
        mValues[1] = z;
    }

    @Override
    public synchronized float[] getCurrentScroll() {
        return mValues;
    }

    @Override
    public synchronized void resetScroll() {
        mValues[0] = 0;
        mValues[1] = 0;
        mValues[2] = 0;
    }

    @Override
    public void setEnable(int type, boolean enable) {
        mEnableValues[type] = enable;
    }

    @Override
    public boolean isEnabled(int type) {
        return mEnableValues[type];
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
    }
}
