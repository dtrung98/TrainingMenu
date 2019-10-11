package com.ldt.parallaximageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ldt.parallaximageview.base.GLTextureView;

public class ParallaxImageView extends GLTextureView implements SensorEventListener, ParallaxRenderer.PositionDeterminer {
    private static final String TAG = "ParallaxImageView";
    public static final int TYPE_ORIGINAL = 0;
    public static final int TYPE_DEPTH = 1;
    private ParallaxRenderer mRenderer;

    public void setOriginalPhoto(Bitmap bitmap) {
        if(mRenderer!=null) {
            mRenderer.setBitmap(bitmap);
            requestLayout();
        }
    }

    Sensor mSensor;
    SensorManager mSensorManager;

    void startSensor() {
        if(getContext()!=null) {
            SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                if(mSensor!=null) {
                    mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                    sensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_GAME);
                }
            }
        }
    }

    void stopSensor() {
        if(mSensor!=null) {
            SensorManager
            mSensor = null;
        }
    }

    /*
       private static class RenderTriggerListener extends TriggerEventListener {

        @Override
        public void onTrigger(TriggerEvent triggerEvent) {

        }
    }

    private RenderTriggerListener mRenderTriggerListener;
    */

    public void setDepthPhoto(Bitmap bitmap) {
        if(mRenderer!=null) {
            mRenderer.setDepthMap(bitmap);
            requestLayout();
        }
    }

    public void removeBitmaps() {
        if(mRenderer!=null) {
            mRenderer.removeBitmaps();
            requestLayout();
        }
    }

    public void setOriginalPath(String uri) {
        setUriBitmap(uri, TYPE_ORIGINAL);
    }

    public void setDepthPath(String uri) {
        setUriBitmap(uri, TYPE_DEPTH);
    }

    private String currentDepthPath = null;
    private String currentOriginalPath = null;

    private void setUriBitmap(final String path,final int type) {
        synchronized (this) {
            if (type == TYPE_ORIGINAL)
                currentOriginalPath = path;
            else currentDepthPath = path;
        }
        if(mRenderer!=null) {
            Glide.with(this)
                    .asBitmap()
                    .load(path)
                   // .override(Math.max(getWidth(), getHeight()))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if(type==TYPE_ORIGINAL&&path.equals(currentOriginalPath))
                            setOriginalPhoto(resource);
                            else if(path.equals(currentDepthPath)) setDepthPhoto(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }
                    });
        }
    }

    public ParallaxImageView(Context context) {
        super(context);
        init(null);
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

   /* public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }*/

    public void init(AttributeSet attrs) {
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        if(attrs!=null&&getContext()!=null) {
            TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.ParallaxImageView);
            setMotionBySensor(t.getBoolean(R.styleable.ParallaxImageView_motionWithSensor,true));
            setMotionByTouch(t.getBoolean(R.styleable.ParallaxImageView_motionWithTouch,true));
            shouldPositionTranslate(t.getBoolean(R.styleable.ParallaxImageView_shouldTranslateByScroll,false));

            t.recycle();
        }
    }

    private boolean mMotionByTouch = true;
    private boolean mMotionBySensor = true;
    private boolean mShouldPositionTranslate = false;

    public boolean isMotionByTouch() {
        return mMotionByTouch;
    }

    public void setMotionByTouch(boolean motionByTouch) {
        mMotionByTouch = motionByTouch;
    }

    public boolean isMotionBySensor() {
        return mMotionBySensor;
    }

    public void setMotionBySensor(boolean motionBySensor) {
        mMotionBySensor = motionBySensor;
    }

    public void registerSensor() {
        if(mSensorManager!=null&&mSensor!=null)
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    public void createRenderer() {
        setEGLContextClientVersion(2);
        mRenderer = new ParallaxRenderer();
        mRenderer.setPositionDeterminer(this);
        mRenderer.shouldPositionTranslate(mShouldPositionTranslate);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

    }


    public void shouldPositionTranslate(boolean active) {
        if(active!= mShouldPositionTranslate) {
            mShouldPositionTranslate = active;
            if(mRenderer!=null) mRenderer.shouldPositionTranslate(mShouldPositionTranslate);
        }

    }

    public void createRenderer(String vertex, String shader) {
        setEGLContextClientVersion(2);
        mRenderer = new ParallaxRenderer(vertex,shader);
        mRenderer.shouldPositionTranslate(mShouldPositionTranslate);
        mRenderer.setPositionDeterminer(this);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        if(mRenderer!=null) {
            Bitmap bitmap = mRenderer.getBitmap();
            Bitmap depth = mRenderer.getDepthMap();
            if (bitmap == null && depth == null) {
                if (w > h) w = h;
                else h = w;
            } else {
                float ratio = 1f;
                if (bitmap != null)
                    ratio = (float) bitmap.getHeight() / bitmap.getWidth();
                else ratio = (float) depth.getHeight() / depth.getWidth();
                h = (int) (ratio*w);
            }
        }
        setMeasuredDimension(w,h);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMotionBySensor)
        startSensor();
    }

    @Override
    public void onPause() {
        stopSensor();
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR ||
                event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
         //   Log.d(TAG, "onSensorChanged: x = "+x+", y = "+ y+", z = "+z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void screenView(int[] size) {
       ViewParent parent =  getParent();
       if(parent instanceof View) {
           size[0] = ((View) parent).getWidth();
           size[1] = ((View) parent).getHeight();
       }
    }

    @Override
    public void currentLocation(int[] location) {
        getLocationInWindow(location);
    }
}
