package com.ldt.vrview.transform;

import android.util.Log;
import android.view.View;

import com.ldt.vrview.gesture.ViewGestureAttacher;

public class GestureTransformer extends BaseTransformer {
    private static final String TAG = "GestureTransformer";

    public ViewGestureAttacher getGestureAttacher() {
        return mAttacher;
    }

    private ViewGestureAttacher mAttacher = new ViewGestureAttacher(this);
    private boolean isAttached = false;
    public GestureTransformer(final int id) {
        super(id);
    }

    @Override
    public void reset() {
        super.reset();
        mXTranslatePixels = 0f;
        mYTranslatePixels = 0f;
        mXViewScale = 1;
        mYViewScale = 1;
        updateTransform();
    }

    private float mXTranslatePixels = 0f;
    private float mYTranslatePixels = 0f;
    private float mXViewScale = 1;
    private float mYViewScale = 1;

    private float wProjectAngle = 0;
    private float hProjectAngle = 0;


    private float mXMinTranslatePixels = 0;
    private float mXMaxTranslatePixels = 0;

    private float mYMinTranslatePixels = 0;
    private float mYMaxTranslatePixels = 0;

    public void postTranslate(float dx, float dy) {
        mXTranslatePixels +=dx;
        mYTranslatePixels +=dy;

        // convert ra goc hay convert ra gia tri ?
        updateTransform();
    }

    public void postScale(float sX, float sY, float pX, float pY) {
        //mXViewTranslate+=pX;
        //mYViewTranslate+=pY;

        mXViewScale *=sX;
        mYViewScale *=sY;

        //mXViewTranslate +=pX;
        //mYViewScale +=pY;

        Log.d(TAG, "post scale with sX = "+sX+", sY = "+sY+", pX = "+pX+", pY = "+pY+" => current scale = "+mXViewScale);
        updateTransform();
    }

    public void setScale(float sX, float sY, float pX, float pY) {
        mXViewScale = sX;
        Log.d(TAG, "set scale with sX = "+sX+", sY = "+sY+", pX = "+pX+", pY = "+pY);
        updateTransform();
    }

    float ratio = 1;

    @Override
    public void updateSize() {
        ratio=(float)mViewWidth/mViewHeight;
        hProjectAngle = 90f;
       // wProjectAngle = (float) (FROM_RADS_TO_DEGS* Math.asin(ratio/1));
        wProjectAngle = 90f*ratio;
        mAttacher.setRangeScroll((int)(120f*mViewWidth/wProjectAngle),(int)(120f*mViewHeight/hProjectAngle));
        mAttacher.setOverScrollRange((int)(15*mViewWidth/wProjectAngle));
    }

    @Override
    public void updateTransform() {
        // left right, up down, and rotation
        mValues[0] = -mXTranslatePixels /mViewWidth*wProjectAngle;
        mValues[1] = mYTranslatePixels /mViewHeight*hProjectAngle;
        mValues[2] = 0;

        mValues[3] = mXViewScale;
        notifyTransformChanged();
    }
    @Override
    public void attach(View view) {
        if(isAttached) detach();
        isAttached = true;
        if(mAttacher!=null) {
            mAttacher.attach(view);
        }
    }

    @Override
    public void detach() {
        isAttached = false;
        if(mAttacher!=null) {
            mAttacher.detach();
        }
    }
}
