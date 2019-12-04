package com.ldt.vrview.transform;

public interface TransformTrackingListener extends TransformListener {
    /**
     *  allow applying the value, or edit it
     * @param which which transformer
     * @param value4 new value
     */
    boolean shouldTransform(int which, float[] value4);
}
