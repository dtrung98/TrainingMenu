package com.zalo.trainingmenu.fundamental.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class OpengGLView extends GLSurfaceView {
    public OpengGLView(Context context) {
        super(context);
        init();
    }

    public OpengGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setRenderer(new BitmapDrawingRenderer());
    }
}
