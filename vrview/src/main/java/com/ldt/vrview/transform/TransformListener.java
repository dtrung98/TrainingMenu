package com.ldt.vrview.transform;

public interface TransformListener {
    /**
     * Notify that the value is changed
     * @param which which transformer
     * @param value4 new value
     */
    void onTransformChanged(int which, float[] value4);

    /**
     * get the possible transform zone
     *
     * @param value8 hMin, hMax, vMin, vMax, rotateMin, rotateMax, scaleMin, scaleMax
     */
    void getTransformZone(float[] value8);//
}
