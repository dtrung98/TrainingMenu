package com.ldt.vrview.transform.base;

import com.ldt.vrview.transform.base.TransformListener;

public interface TrackingTransformListener extends TransformListener {

    /**
     * get the possible transform zone
     *
     * @param value8 hMin, hMax, vMin, vMax, rotateMin, rotateMax, scaleMin, scaleMax
     */
    void getTransformZone(float[] value8);

    /**
     *
     * @param value4
     */
    void getCurrentTransform(float[] value4);
}
