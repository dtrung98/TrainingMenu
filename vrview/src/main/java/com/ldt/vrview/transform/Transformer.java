package com.ldt.vrview.transform;

public interface Transformer {
    public static final int TYPE_ROTATE_X = 0;
    public static final int TYPE_ROTATE_Y = 1;
    public static final int TYPE_ROTATE_Z = 2;
    public static final int TYPE_SCALE = 3;

    void setViewSize(int width, int height);
    void setTextureSize(float width, float height);

    void setValue(int type, float value);
    void scrollBy(float x, float y, float z);
    void scrollBy(float[] values);
    void scrollXBy(float x);
    void scrollYBy(float y);
    void scrollZBy(float z);
    float[] getCurrentScroll();
    void reset();
    void setEnable(int type, boolean enable);
    boolean isEnabled(int type);
    void updateSize();
    void updateTransform();
}
