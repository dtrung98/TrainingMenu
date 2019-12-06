package com.ldt.vrview.transform;

import android.util.Log;
import android.view.View;

import com.ldt.vrview.gesture.ViewGestureAttacher;
import com.ldt.vrview.transform.base.ChildTransformer;

public class GestureTransformer extends ChildTransformer {
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

        mXViewScale = 1;
        mYViewScale = 1;
        updateTransform();
    }

    private float mXViewScale = 1;
    private float mYViewScale = 1;

    private float wProjectAngle = 0;
    private float hProjectAngle = 0;

    float[] mParentValue = new float[4];
    float[] mParentTransformZone;

    float mTempAngleX, mTempAngleY;

    public void postTranslate(float dx, float dy) {

        getTransformListener().getCurrentTransform(mParentValue);
        if(mParentTransformZone==null) {
            mParentTransformZone = new float[8];
        }
            getTransformListener().getTransformZone(mParentTransformZone); // minX, maxX, minY, maxY,

        halfScreenXAngle = 30;//convertPixelToAngleX(mViewWidth/2);
        halfScreenYAngle = convertPixelToAngleY(mViewHeight/2);

        mParentTransformZone[0] += halfScreenXAngle;
        mParentTransformZone[1] -= halfScreenXAngle;
        mParentTransformZone[2] += halfScreenYAngle;
        mParentTransformZone[3] -= halfScreenYAngle;
        Log.d(TAG, "halfScreenXAngle = "+halfScreenXAngle+", halfScreenYAngle = "+halfScreenYAngle +" when w = "+mViewWidth+", h = "+mViewHeight+", ratio = "+ratio);
            Log.d(TAG, "transform after combined with screen: minX = "+mParentTransformZone[0]+", maxX = "+mParentTransformZone[1]+", minY = "+mParentTransformZone[2]+", maxY = "+mParentTransformZone[3]);

        float dAngleX = - convertPixelToAngleX(dx);
        float dAngleY = convertPixelToAngleY(dy);

        float diffAngleX = mParentValue[0] - mValues[0];
        float diffAngleY = mParentValue[1] - mValues[1];

        mTempAngleX = mValues[0] + dAngleX;
        mTempAngleY = mValues[1] + dAngleY;



        if(mTempAngleX + diffAngleX > mParentTransformZone[1]) {
            // over max x
            mTempAngleX = mParentTransformZone[1] - diffAngleX;
        } else if(mTempAngleX + diffAngleX<mParentTransformZone[0]) {
            // over min x
            mTempAngleX = mParentTransformZone[0] - diffAngleX;
        }



        if(mTempAngleY + diffAngleY > mParentTransformZone[3]) {
            // over max y
            mTempAngleY = mParentTransformZone[3] - diffAngleY;
        } else if(mTempAngleY + diffAngleY < mParentTransformZone[2]) {
            // over min y
            mTempAngleY = mParentTransformZone[2] - diffAngleY;
        }


        Log.d(TAG, "next transform will be: valueX = "+mTempAngleX+diffAngleX+", valueY = "+mTempAngleY+diffAngleY);
        synchronized (this) {
            mValues[0] = mTempAngleX;
            mValues[1] = mTempAngleY;
            mValues[2] = 0;
            //mXTranslatePixels +=dx;
            //mYTranslatePixels +=dy;
        }

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
    private float halfScreenYAngle;
    private float halfScreenXAngle;

    @Override
    public void updateSize() {
        ratio=mViewWidth/mViewHeight;
        hProjectAngle = 90f;
       // wProjectAngle = (float) (FROM_RADS_TO_DEGS* Math.asin(ratio/1));
        wProjectAngle = 90f*ratio;
        mAttacher.setRangeScroll((int)(720f*mViewWidth/wProjectAngle),(int)(720f*mViewHeight/hProjectAngle));
        mAttacher.setOverScrollRange((int)(15*mViewWidth/wProjectAngle));
    }

    public float convertPixelToAngleX(float dx) {

        // false
        return dx/mViewWidth*wProjectAngle; // == dx /mViewWidth * 90 *mViewWidth /mViewHeight == dx / mViewHeight * 90
    }

    public float convertPixelToAngleY(float dy) {
        return dy/mViewHeight*hProjectAngle; // true
    }

    @Override
    public void updateTransform() {
        // left right, up down, and rotation

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
