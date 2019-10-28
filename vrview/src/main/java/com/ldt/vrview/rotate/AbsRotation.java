package com.ldt.vrview.rotate;

public interface AbsRotation {
    void scrollBy(float x, float y, float z);
    void scrollBy(float[] values);
    void scrollXBy(float x);
    void scrollYBy(float y);
    void scrollZBy(float z);
    float[] getCurrentScroll();
    void resetScroll();
}
