package com.ldt.vrview.transform.base;

public interface TransformListener {
    /**
     * Notify that the value is changed
     * @param which which transformer
     * @param value4 new value of this child transformer
     */
    void onTransformChanged(int which, float[] value4);
}
