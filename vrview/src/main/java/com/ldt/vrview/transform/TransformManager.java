package com.ldt.vrview.transform;

import android.view.View;

import com.ldt.vrview.gesture.ViewGestureAttacher;

import java.util.ArrayList;

public final class TransformManager extends BaseTransformer implements TransformListener {
    private static final String TAG = "TransformManager";
    public static final int GESTURE_TRANSFORMER = 1;
    public static final int SENSOR_TRANSFORMER = 2;
    public static final int TRANSFORM_MANAGER = 3;

    private ArrayList<BaseTransformer> mTransformers = new ArrayList<>();
    private SensorTransformer mSensorTransformer;
    private GestureTransformer mGestureTransformer;


    public TransformManager(int id) {
        super(id);
        mGestureTransformer = new GestureTransformer(GESTURE_TRANSFORMER);
        mSensorTransformer = new SensorTransformer(SENSOR_TRANSFORMER);
        mTransformers.add(mGestureTransformer);
        mTransformers.add(mSensorTransformer);

    }

    @Override
    public void setViewSize(int width, int height) {
        for (int i = 0; i < mTransformers.size(); i++) {
           mTransformers.get(i).setViewSize(width,height);
        }
    }

    @Override
    public void setTextureSize(float width, float height) {
        super.setTextureSize(width, height);
    }

    @Override
    public void updateTransform() {
        float[] v = new float[3];
        for (int i = 0; i < mTransformers.size(); i++) {
            v[0]+=mTransformers.get(i).mValues[0];
            v[1]+=mTransformers.get(i).mValues[1];
            v[2]+=mTransformers.get(i).mValues[2];
        }


        System.arraycopy(v,0,mValues,0,3);

    }

    @Override
    public void reset() {
        super.reset();
        for (int i = 0; i < mTransformers.size(); i++) {
            mTransformers.get(i).reset();
        }
        updateTransform();
        notifyTransformChanged();
    }

    @Override
    public void attach(View view) {
        for (int i = 0; i < mTransformers.size(); i++) {
            mTransformers.get(i).setTransformListener(this);
            mTransformers.get(i).attach(view);
        }
    }

    @Override
    public void detach() {
        for (int i = 0; i < mTransformers.size(); i++) {
            mTransformers.get(i).setTransformListener(null);
            mTransformers.get(i).detach();
        }
    }

    public ViewGestureAttacher getGestureAttacher() {
        if(mGestureTransformer!=null) return mGestureTransformer.getGestureAttacher();
        return null;
    }

    @Override
    public void onTransformChanged(int which, float[] angle3) {
        updateTransform();
        notifyTransformChanged();
    }
}
