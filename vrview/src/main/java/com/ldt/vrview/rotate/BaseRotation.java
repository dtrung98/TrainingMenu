package com.ldt.vrview.rotate;

public class BaseRotation implements AbsRotation {
    private float[] mValues = new float[] {0,0,0};

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
}
