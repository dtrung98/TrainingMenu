package com.ldt.vrview;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;

import com.ldt.vrview.gesture.ViewGestureAttacher;
import com.ldt.vrview.model.VRPhoto;
import com.ldt.vrview.transform.TransformListener;
import com.ldt.vrview.transform.TransformManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VRControlView extends GLTextureView implements GLTextureView.Renderer, GLSurfaceView.Renderer, TransformListener {
    public int id = 0;
    private PanoramaSphere mSphere;
    TransformManager mTransformManager = new TransformManager(TransformManager.TRANSFORM_MANAGER);

    public VRControlView(Context context) {
        super(context);
        init(null);
    }



    public VRControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VRControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void recalibrate() {
        mTransformManager.reset();
    }

    private void init(AttributeSet attrs) {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mSphere = new PanoramaSphere(this, getContext());
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable._360sp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSphere.mBitmap = bitmap;
        onChangeOrientation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTransformManager.setTransformListener(this);
        mTransformManager.attach(this);
    }

    @Override
    public void onPause() {
        mTransformManager.detach();
        mTransformManager.setTransformListener(null);
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
        mTransformManager.setViewSize(width,height);
        float[] textureSize = new float[2];
        mSphere.getTextureCoordSize(textureSize);
        mTransformManager.setTextureSize(textureSize[0],textureSize[1]);
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
        mSphere.setVRPhoto(null);
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

    public void setVRPhoto(VRPhoto vrio) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mTransformManager.reset();
                if(mSphere!=null) mSphere.setVRPhoto(vrio);
            }
        });

    }

    public ViewGestureAttacher getGestureAttacher() {
        if(mTransformManager!=null) return mTransformManager.getGestureAttacher();
        return null;
    }

    @Override
    public void onTransformChanged(int which, float[] angle3) {
        mSphere.setTransformValue(angle3);
    }

    public void setViewID(int id) {
        this.id = id;
      //  mSphere.id = id;
    }
}
