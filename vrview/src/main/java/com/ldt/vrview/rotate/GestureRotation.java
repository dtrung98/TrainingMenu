package com.ldt.vrview.rotate;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.ldt.vrview.gesture.ViewGestureAttacher;

public class GestureRotation extends BaseRotation {

    private ViewGestureAttacher mAttacher = new ViewGestureAttacher(this);
    private boolean isAttached = false;
    public GestureRotation() {
    }

    public void init(View view) {
        if(isAttached) destroy();
        isAttached = true;
        mAttacher.attach(view);
    }

    public void destroy() {
        isAttached = false;
        mAttacher.detach();
    }
}
