package com.ldt.vrview;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VRControlView extends GLTextureView implements GLTextureView.Renderer, GLSurfaceView.Renderer, SensorEventListener {
    private Sphere mSphere;

    private SensorManager mSensorManager;
    private Sensor mSensorRotation;

    public VRControlView(Context context) {
        super(context);
        init(null);
    }



    public VRControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

   /* public VRControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }*/

    public void recalibrate() {
        if (mSphere != null) mSphere.recalibrate();
    }

    private void init(AttributeSet attrs) {
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager != null) {
            // List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            //todo 判断是否存在rotation vector sensor
            mSensorRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mSphere = new Sphere(this, getContext(), R.drawable._360sp);
        onChangeOrientation();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSensorManager != null)
            mSensorManager.registerListener(this, mSensorRotation, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public void onPause() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSphere.create();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);
    }

    private float w, h;

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSphere.setSize(width, height);
        w = width;
        h = height;
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1, 1, 1, 1);
        mSphere.draw();
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //SensorManager.getRotationMatrixFromVector(matrix,event.values);
        // mSkySphere.setMatrix(matrix,event.values);
        mSphere.setVector(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onChangeOrientation();

    }

    private void onChangeOrientation() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            mSphere.onChangeOrientation(display.getRotation());
        }
    }
}
