package com.ldt.vrview.transform;

import android.view.View;

import com.ldt.vrview.VRControlView;
import com.ldt.vrview.gesture.ViewGestureAttacher;

public class GestureTransformer extends BaseTransformer implements View.OnClickListener {

    private ViewGestureAttacher mAttacher = new ViewGestureAttacher(this);
    private boolean isAttached = false;
    private View mView;
    public GestureTransformer() {
    }

    @Override
    public synchronized void reset() {
        super.reset();
        mXViewTranslate = 0f;
        mYViewTranslate = 0f;
        updateTransform();
    }

    private float mXViewTranslate = 0f;
    private float mYViewTranslate = 0f;

    private float wProjectAngle = 0;
    private float hProjectAngle = 0;

    public void postTranslate(float dx, float dy) {
        mXViewTranslate+=dx;
        mYViewTranslate+=dy;

        // convert ra goc hay convert ra gia tri ?
        updateTransform();
    }

    public void postScale(float sX, float sY, float pX, float pY) {
        updateTransform();
    }

    public void setScale(float sX, float sY, float pX, float pY) {
        updateTransform();
    }

    float ratio = 1;

    @Override
    public void updateSize() {
        ratio=(float)mViewWidth/mViewHeight;
        hProjectAngle = 90f;
       // wProjectAngle = (float) (FROM_RADS_TO_DEGS* Math.asin(ratio/1));
        wProjectAngle = 90f*ratio;
        mAttacher.setRangeScroll((int)(720f*mViewWidth/wProjectAngle),(int)(720f*mViewHeight/hProjectAngle));
        mAttacher.setOverScrollRange((int)(15*mViewWidth/wProjectAngle));
    }

    @Override
    public synchronized void updateTransform() {
        // left right, up down, and rotation
        mValues[0] = -mXViewTranslate/mViewWidth*wProjectAngle;
        mValues[1] = mYViewTranslate/mViewHeight*hProjectAngle;
        mValues[2] = 0;

    }
    @Override
    public void attach(View view) {
        if(isAttached) detach();
        isAttached = true;
        if(mAttacher!=null) {
            mAttacher.attach(view);
            mView = view;
            mAttacher.setOnClickListener(this);
        }
    }

    @Override
    public void detach() {
        isAttached = false;
        if(mAttacher!=null) {
            mAttacher.detach();
            mView = null;
            mAttacher.setOnClickListener(null);
        }
    }

    @Override
    public void onClick(View v) {
        if(mView instanceof VRControlView) {
            ((VRControlView) mView).recalibrate();
        }
    }
}
