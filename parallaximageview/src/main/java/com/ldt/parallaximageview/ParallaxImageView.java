package com.ldt.parallaximageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ldt.parallaximageview.base.GLTextureView;
import com.ldt.parallaximageview.model.ParallaxImageObject;
import com.ldt.parallaximageview.util.Util;

import java.lang.ref.WeakReference;

public class ParallaxImageView extends GLTextureView implements SensorEventListener, ParallaxRenderer.PositionDeterminer {
    private static final String TAG = "ParallaxImageView";
    public static final int TYPE_ORIGINAL = 0;
    public static final int TYPE_DEPTH = 1;
    private final ParallaxRenderer mRenderer = new ParallaxRenderer();
    private String mName = "";
    public void getColor(float[] color) {
        mRenderer.getColor(color);
    }

    public void setBackColor(float r, float g, float b, float a) {
        mRenderer.setBackColor(r,g,b,a);
    }

    public void setBackColor(int color) {
        mRenderer.setBackColor(Color.red(color)/255f,Color.green(color)/255f,Color.blue(color)/255f,Color.alpha(color)/255f);
    }

    public void setOriginalPhoto(Bitmap bitmap) {
            mRenderer.setBitmap(bitmap);
            Log.d(TAG, "view "+mName+" set original"+((bitmap == null) ? ", null" : ", not null"));
            requestLayout();
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
        mRenderer.setDepthMap(bitmap);
        Log.d(TAG, "view "+mName+" set depth"+((bitmap == null) ? ", null" : ", not null"));

        requestLayout();
    }

    private void requestUpdateBitmaps() {
    }

    public void removeBitmaps() {
        mRenderer.removeBitmaps();
        requestLayout();
    }

    public void loadOriginal(Object uri) {
        Glide.with(this)
                .asBitmap()
                .load(uri)
                .error(R.drawable.error_cloud)
                .into(mOriginalTarget);
    }

    public void setDepthPath(String uri) {
        Glide.with(this)
                .asBitmap()
                .load(uri).into(mDepthTarget);
    }

    ParallaxImageObject mPio;

    public void load(ParallaxImageObject pio) {
        mPio = pio;
        if(pio!=null) {
            if (pio.getOriginal() instanceof Bitmap)
                setOriginalPhoto((Bitmap) pio.getOriginal());
            else
                Glide.with(getContext())
                        .asBitmap()
                        .load(pio.getOriginal())
                        //.error(Glide.with(this).asBitmap().load(pio.getOriginal()))
                        .into(mOriginalTarget.setLoadObject(pio.getOriginal()));

            if (pio.getDepth() instanceof Bitmap)
                setDepthPhoto((Bitmap) pio.getDepth());
            else
                Glide.with(getContext())
                        .asBitmap()
                        .load(pio.getDepth())
                        // .override(Target.SIZE_ORIGINAL)
                        //.error(Glide.with(this).asBitmap().load(pio.getOriginal()))
                        .into(mDepthTarget.setLoadObject(pio.getDepth()));
        }
    }

    private ParallaxTarget mOriginalTarget = new ParallaxTarget(this,TYPE_ORIGINAL);

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    private static class ParallaxTarget extends CustomTarget<Bitmap> {

        public ParallaxTarget(ParallaxImageView view, int type) {
            super();
            mRef = new WeakReference<>(view);
            mType = type;
        }


        private final WeakReference<ParallaxImageView> mRef;

        public Object getLoadObject() {
            return mLoadObject;
        }

        public ParallaxTarget setLoadObject(Object loadObject) {
            mLoadObject = loadObject;
            return this;
        }

        private Object mLoadObject;
        private final int mType;

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            Log.d(TAG, "onResourceReady: object ["+mLoadObject.toString()+"] ready");
            if(mRef.get()!=null) {
                if(mType==TYPE_ORIGINAL)
                mRef.get().setOriginalPhoto(resource);
                else mRef.get().setDepthPhoto(resource);
            }
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            mRef.get().removeBitmaps();
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            Log.d(TAG, "onResourceReady: object ["+mLoadObject+"] failed");
            if(mRef.get()!=null) {
                if(mType==TYPE_ORIGINAL)
                    mRef.get().setOriginalPhoto(null);
                else mRef.get().setDepthPhoto(null);
            }
        }
    }


    private ParallaxTarget mDepthTarget = new ParallaxTarget(this,TYPE_DEPTH);


    public ParallaxImageView(Context context) {
        super(context);
        init(null);
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

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
            int color = t.getColor(R.styleable.ParallaxImageView_backColor,-1);
            if(color!=1) setBackColor(color);
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



    public void initRenderer() {
        setEGLContextClientVersion(2);
        mRenderer.init();
        mRenderer.setPositionDeterminer(this);
        mRenderer.shouldPositionTranslate(mShouldPositionTranslate);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

    }


    public void shouldPositionTranslate(boolean active) {
        if(active!= mShouldPositionTranslate) {
            mShouldPositionTranslate = active;
            mRenderer.shouldPositionTranslate(mShouldPositionTranslate);
        }

    }

    public void initRenderer(String vertex, String shader) {
        setEGLContextClientVersion(2);
        mRenderer.init(vertex,shader);
        mRenderer.setPositionDeterminer(this);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

   /*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        if(mRenderer!=null) {
            Bitmap bitmap = mRenderer.getBitmap();
            Bitmap depth = mRenderer.getDepthMap();
            if (bitmap == null && depth == null) {
                h = w;
            } else {
                float ratio = 1f;
                if (bitmap != null)
                    ratio = (float) bitmap.getHeight() / bitmap.getWidth();
                else ratio = (float) depth.getHeight() / depth.getWidth();
                h = (int) (ratio*w);
            }
        }
        Log.d(TAG, "onMeasure: w = "+w+", h = "+h);
        setMeasuredDimension(w,h);
    }
    */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        if(mRenderer!=null) {
            Bitmap bitmap = mRenderer.getBitmap();
            if (bitmap == null) {
                h = 0;
            } else {
                float ratio = 1f;
             //   if (bitmap != null)
                    ratio = (float) bitmap.getHeight() / bitmap.getWidth();
             //   else ratio = (float) depth.getHeight() / depth.getWidth();
                h = (int) (ratio*w);
            }
        }
        Log.d(TAG, "onMeasure: w = "+w+", h = "+h);
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
        Util.getScreenSize(getContext(),size);
      /*
       ViewParent parent =  getParent();
       if(parent instanceof View) {
           size[0] = ((View) parent).getWidth();
           size[1] = ((View) parent).getHeight();
       }
       */
    }

    @Override
    public void currentLocation(int[] location) {
        getLocationInWindow(location);
        Log.d(TAG, "currentLocation: view "+mName+" with location ["+location[0]+", "+location[1]+"), bitmap "
                +((mRenderer.getBitmap()==null) ? "is null" :"exists") +" and depth "
                +((mRenderer.getDepthMap()==null) ? "is null" :"exists")
        );
    }
}
