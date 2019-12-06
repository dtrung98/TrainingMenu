package com.ldt.vrview.transform;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

import com.ldt.vrview.transform.base.BaseTransformer;
import com.ldt.vrview.transform.base.ChildTransformer;

public class SensorTransformer extends ChildTransformer implements SensorEventListener{
    private static final String TAG = "SensorTransformer";
    public static final float FROM_RADS_TO_DEGS = 57.2957795f;

    private float[] initSensorM = new float[16];
    private float[] currentSensorM = new float[16];
    private float[] curSensorM_vs_InitSensorM = new float[3];
    private View mView;
    SensorManager mSensorManager;
    Sensor mSensor;
    private int mLastAccuracy;
    private boolean mIsInitSensor = false;

    public SensorTransformer(final int id){
        super(id);
    }

    @Override
    public void reset() {
        super.reset();
        mIsInitSensor = false;
    }

    @Override
    public void attach(View view) {
        super.attach(view);
        if (mView != null) detach();
        mView = view;
        if (view != null) {
            mSensorManager = (SensorManager) view.getContext().getSystemService(Context.SENSOR_SERVICE);
            if(mSensorManager!=null)
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        if(mSensorManager!=null)
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public void detach() {
        mView  = null;
        if(mSensorManager!=null) {
            mSensorManager.unregisterListener(this);
        }
        mSensor = null;
        mSensorManager = null;
        super.detach();
    }

    private float[] tempVector = new float[4];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == mSensor) {
            if (event.values.length > 4) {
                System.arraycopy(event.values, 0, tempVector, 0, 4);
                onOrientationChanged(tempVector);
            } else
            onOrientationChanged(event.values);
        }
    }

    private void onOrientationChanged(float[] rotationVector) {
        if(!mIsInitSensor) {
            mIsInitSensor = true;
            SensorManager.getRotationMatrixFromVector(initSensorM,rotationVector);
        }

        SensorManager.getRotationMatrixFromVector(currentSensorM,rotationVector);
        SensorManager.getAngleChange(curSensorM_vs_InitSensorM,currentSensorM,initSensorM);

        curSensorM_vs_InitSensorM[0] *= FROM_RADS_TO_DEGS;
        curSensorM_vs_InitSensorM[1] *= FROM_RADS_TO_DEGS;
        curSensorM_vs_InitSensorM[2] *= FROM_RADS_TO_DEGS;

        updateTransform();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    @Override
    public void updateTransform() {
        mValues[0] = -curSensorM_vs_InitSensorM[2];
        mValues[1] = -curSensorM_vs_InitSensorM[1];
        mValues[2] = -curSensorM_vs_InitSensorM[0];
        notifyTransformChanged();
    }
}
